package com.cbl.report.consts;

public class ExceptionMsg {
    public static final String MISSING_BEARER_TOKEN = "Bearer token is missing in the authorization header.";
    public static final String INVALID_BEARER_TOKEN = "Invalid Token. Auth header is not starts with 'Bearer'";
    public static final String TOKEN_EXPIRED = "Token is expired.";
    public static final String UNSUPPORTED_AUTHORITY_CLAIM = "Unsupported authorities claim type";
    public static final String SERVICE_UNAVAILABLE = "Requested service unavailable. Please try again later.";
    public static final String GATEWAY_TIMEOUT = "Gateway timeout. Please try again later.";
    public static final String USER_NOT_FOUND_DURING_TOKEN_VALIDATION = "The username or email: '%s' does not exist in DB during token validation.";
}
