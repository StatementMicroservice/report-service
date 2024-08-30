package com.cbl.report.config.appconfig;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "report-service")
public class AppConfigProperty {
    private String serverPort;
    private String activeProfile;
    private String testValue;
    private String jwtSecret;
    private String accessTokenExpiry;
    private String refreshTokenExpiry;
    private AuthService authService;

    @Data
    public static class AuthService {
        private String baseUrl;
        private String userDetailsEndpoint;
    }
}
