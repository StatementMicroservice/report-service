package com.cbl.report.config.securityconfig.jwtconfig;

import com.cbl.report.consts.ExceptionMsg;
import com.cbl.report.exc.AuthorizationHeaderNotFoundException;
import com.cbl.report.exc.TokenNotFoundException;
import com.cbl.report.response.AuthServiceApiResponse;
import com.cbl.report.response.ResponseHandler;
import com.cbl.report.util.CommonUtility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtAccessTokenFilter extends OncePerRequestFilter {
    private final JwtTokenUtils jwtTokenUtils;
    private final JwtDecoder jwtDecoder;
    private final ObjectMapper objectMapper = new ObjectMapper()
                                                      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                                      .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final var errors = new ArrayList<Map<String, String>>();
        try {
            log.info("[JwtAccessTokenFilter:doFilterInternal] :: Started Filtering the Http Request:{}", request.getRequestURI());
            final String authHeader = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                                              .orElseThrow(() -> new AuthorizationHeaderNotFoundException(ExceptionMsg.MISSING_BEARER_TOKEN));

            if (!authHeader.startsWith(OAuth2AccessToken.TokenType.BEARER.getValue())) {
                errors.add(Map.of("message", ExceptionMsg.INVALID_BEARER_TOKEN));
                log.info("[JwtAccessTokenFilter:doFilterInternal] Token is invalid.");

                ResponseHandler.sendResponse(false
                                           , HttpStatus.UNAUTHORIZED
                                           , Strings.EMPTY
                                           , response
                                           , Optional.empty()
                                           , errors);
            }

            final String token = authHeader.substring(7);
            final Jwt jwtToken = jwtDecoder.decode(token);
            final String userName = jwtTokenUtils.getUserName(jwtToken);

            if (!userName.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
                final var userNameFromDB = Optional.ofNullable(jwtTokenUtils.userDetails(userName, response))
                                                   .orElseThrow(() -> new UsernameNotFoundException(String.format(ExceptionMsg.USER_NOT_FOUND_DURING_TOKEN_VALIDATION,userName)));

                if (jwtTokenUtils.isTokenValid(jwtToken, userNameFromDB)) {
                    final var securityContext = SecurityContextHolder.createEmptyContext();
                    final var authorities = jwtTokenUtils.getAuthoritiesFromJwt(jwtToken);

                    final var createdToken = new UsernamePasswordAuthenticationToken(userName
                                                                                   , null
                                                                                   , authorities);
                    createdToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    securityContext.setAuthentication(createdToken);
                    SecurityContextHolder.setContext(securityContext);
                    log.info("[JwtAccessTokenFilter:doFilterInternal] Token is valid, setting security context.");
                } else {
                    errors.add(Map.of("message", ExceptionMsg.TOKEN_EXPIRED));
                    log.info("[JwtAccessTokenFilter:doFilterInternal] Token is expired.");

                    ResponseHandler.sendResponse(false
                                                , HttpStatus.UNAUTHORIZED
                                                , Strings.EMPTY
                                                , response
                                                , Optional.empty()
                                                , errors);
                }
                log.info("[JwtAccessTokenFilter:doFilterInternal] Completed");
                filterChain.doFilter(request, response);
            }
        } catch (BadJwtException | AuthorizationHeaderNotFoundException | TokenNotFoundException e) {
            if (e instanceof JwtValidationException) {
                errors.add(Map.of("message", ExceptionMsg.TOKEN_EXPIRED));
            }else {
                errors.add(Map.of("message", e.getMessage()));
            }
            ResponseHandler.sendResponse(false
                                        , HttpStatus.UNAUTHORIZED
                                        , Strings.EMPTY
                                        , response
                                        , Optional.empty()
                                        , errors);
        } catch (HttpClientErrorException e) {
            if(e.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                sendResponseForBadRequestException(response, e);
            }
            final var apiResponse = convertApiResponse(e.getResponseBodyAsString());

            ResponseHandler.sendResponse(false
                                       , HttpStatus.valueOf(apiResponse.getCode())
                                       , Strings.EMPTY
                                       , response
                                       , Optional.empty()
                                       , apiResponse.getErrors());
        } catch (RestClientException e) {
            final HttpStatus httpStatus = getAppropriateStatus(e.getCause());

            if(httpStatus.isSameCodeAs(HttpStatus.SERVICE_UNAVAILABLE)) {
                errors.add(Map.of("message", ExceptionMsg.SERVICE_UNAVAILABLE));
            } else if(httpStatus.isSameCodeAs(HttpStatus.GATEWAY_TIMEOUT)) {
                errors.add(Map.of("message", ExceptionMsg.GATEWAY_TIMEOUT));
            }else {
                errors.add(Map.of("message", e.getMessage()));
            }

            ResponseHandler.sendResponse(false
                                       , HttpStatus.valueOf(httpStatus.value())
                                       , Strings.EMPTY
                                       , response
                                       , Optional.empty()
                                       , errors);
        } catch (UsernameNotFoundException e) {
            errors.add(Map.of("message", e.getMessage()));

            ResponseHandler.sendResponse(false
                                       , HttpStatus.NOT_FOUND
                                       , Strings.EMPTY
                                       , response
                                       , Optional.empty()
                                       , errors);
        }
        catch (Exception e) {
            errors.add(Map.of("message", e.getMessage()));
            ResponseHandler.sendResponse(false
                                        , HttpStatus.INTERNAL_SERVER_ERROR
                                        , Strings.EMPTY
                                        , response
                                        , Optional.empty()
                                        , errors);
        }
    }

    private void sendResponseForBadRequestException(HttpServletResponse response, HttpClientErrorException e) throws Exception {
        final List<Map<String, String>> errors = new ArrayList<>();
        final String jsonString = CommonUtility.extractJsonString(e.getMessage());
        final ErrorMessage errorMessage = extractBadRequestErrorMessage(jsonString);

        errors.add(Map.of("message", errorMessage.getError()));

        ResponseHandler.sendResponse(false
                                   , HttpStatus.valueOf(e.getStatusCode().value())
                                   , Strings.EMPTY
                                   , response
                                   , Optional.empty()
                                   , errors);
    }

    private HttpStatus getAppropriateStatus(Throwable cause) {
        final HttpStatus httpStatus;
        if (cause instanceof ConnectException) {
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
        } else if (cause instanceof SocketTimeoutException) {
            httpStatus = HttpStatus.GATEWAY_TIMEOUT;
        } else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return httpStatus;
    }

    private ErrorMessage extractBadRequestErrorMessage(String jsonString) throws JsonProcessingException {
        return objectMapper.readValue(jsonString, ErrorMessage.class);
    }


    private AuthServiceApiResponse<?> convertApiResponse(String responseBodyAsString) throws JsonProcessingException {
        return objectMapper.readValue(responseBodyAsString, AuthServiceApiResponse.class);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorMessage {
        @JsonProperty("error")
        private String error;
    }
}
