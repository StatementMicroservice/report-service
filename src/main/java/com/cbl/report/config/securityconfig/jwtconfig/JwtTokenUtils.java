package com.cbl.report.config.securityconfig.jwtconfig;

import com.cbl.report.config.appconfig.AppConfigProperty;
import com.cbl.report.consts.ExceptionMsg;
import com.cbl.report.response.AuthServiceApiResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenUtils {

    private final AppConfigProperty appConfigProperty;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper()
                                                      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                                      .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

    public String getUserName(Jwt jwtToken) {
        return jwtToken.getSubject();
    }

    public boolean isTokenValid(Jwt jwtToken, String userNameFromDB) {
        final String userNameFromToken = getUserName(jwtToken);
        final boolean isTokenExpired = isTokenExpired(jwtToken);
        final boolean isTokenUserSameAsDatabase = userNameFromToken.equals(userNameFromDB);
        return !isTokenExpired && isTokenUserSameAsDatabase;
    }

    public boolean isTokenExpired(Jwt jwtToken) {
        return Objects.requireNonNull(jwtToken.getExpiresAt()).isBefore(Instant.now());
    }

    public Claims getClaims(final String token) {
        final Key key = Keys.hmacShaKeyFor(appConfigProperty.getJwtSecret().getBytes());
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public Collection<GrantedAuthority> getAuthoritiesFromJwt(Jwt jwtToken) {
        final var authoritiesClaim = jwtToken.getClaims().get("scope");
        if (authoritiesClaim instanceof String authoritiesString) {
            return Arrays.stream(authoritiesString.split(","))
                         .map(SimpleGrantedAuthority::new)
                         .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(ExceptionMsg.UNSUPPORTED_AUTHORITY_CLAIM);
        }
    }

    public String userDetails(String emailId, HttpServletResponse response) throws Exception {

            final ResponseEntity<Object> apiResponse = restTemplate.exchange(getUserDetailsApiUri(emailId)
                                                                           , HttpMethod.GET
                                                                           , null
                                                                           , new ParameterizedTypeReference<>() {});

            return parseUserInfoFromResponse(apiResponse);
    }

    private String getUserDetailsApiUri(String emailId) {
        final var baseUrl = appConfigProperty.getAuthService()
                                             .getBaseUrl();
        final var userDetailsEndpoint = appConfigProperty.getAuthService()
                                                         .getUserDetailsEndpoint();

        return UriComponentsBuilder.fromUriString(baseUrl + userDetailsEndpoint)
                                   .queryParam("emailId", emailId)
                                   .toUriString();
    }


    private String parseUserInfoFromResponse(ResponseEntity<Object> apiResponse) throws JsonProcessingException {
        final var responseBody = apiResponse.getBody();
        final var jsonData = objectMapper.writeValueAsString(responseBody);
        final var authServiceApiResponse = objectMapper.readValue(jsonData, AuthServiceApiResponse.class);
        final var userInfoJsonData = objectMapper.writeValueAsString(authServiceApiResponse.getData());
        final var userInfo = objectMapper.readValue(userInfoJsonData, UserInfo.class);

        return userInfo.getUserName();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserInfo {
        @JsonProperty("userName")
        private String userName;
    }
}
