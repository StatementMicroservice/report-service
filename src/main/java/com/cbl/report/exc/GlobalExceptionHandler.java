package com.cbl.report.exc;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<?> handleAllException(Exception ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public final ResponseEntity<?> handleAllRuntimeException(RuntimeException ex) {
        Map<String, Object> responseBody = getResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public final ResponseEntity<?> authorizationDeniedException(AuthorizationDeniedException ex) {
        Map<String, Object> responseBody = getResponseEntity(ex.getMessage(), HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase());
        return new ResponseEntity<>(responseBody, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> validationException(ValidationException exception) {
        return ResponseEntity.badRequest().body(exception.getValidationErrors());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> responseStatusException(ResponseStatusException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getReason());
        return new ResponseEntity<>(errors, ex.getStatusCode());
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<?> tokenNotFoundException(TokenNotFoundException ex) {
        Map<String, Object> responseBody = getResponseEntity(ex.getMessage()
                , HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase());
        return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthorizationHeaderNotFoundException.class)
    public ResponseEntity<?> authorizationHeaderNotFoundException(AuthorizationHeaderNotFoundException ex) {
        Map<String, Object> responseBody = getResponseEntity(ex.getMessage()
                , HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
        return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
    }

    private Map<String, Object> getResponseEntity(String message, int statusCode, String statusTitle) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("success", false);
        responseBody.put("error_code", statusCode);
        responseBody.put("error_title", statusTitle);
        responseBody.put("message", message);

        return responseBody;
    }
}
