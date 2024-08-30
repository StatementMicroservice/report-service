package com.cbl.report.controller;

import com.cbl.report.config.appconfig.AppConfigProperty;
import com.cbl.report.util.UrlUtility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping(UrlUtility.DUMMY_DASHBOARD_CONTROLLER)
public class DummyDashboardController {

    private static final Logger log = LoggerFactory.getLogger("dashboardLogger");

    private final AppConfigProperty appConfigProperty;

    @PreAuthorize("hasAuthority('SCOPE_READ')")
    @GetMapping("/welcome-message")
    public ResponseEntity<String> getFirstWelcomeMessage() {
        return ResponseEntity.ok("Welcome to the JWT :" + "with scope:");
    }

    @PreAuthorize("hasAuthority('SCOPE_READ')")
    @GetMapping("/manager-message")
    public ResponseEntity<String> getManagerData(Principal principal) {
        log.info("TestValue is : {}", appConfigProperty.getTestValue());
        return ResponseEntity.ok("Manager::" + principal.getName() + " [city-statement-auth-service] . " + appConfigProperty.getTestValue());

    }

    @PreAuthorize("hasAuthority('SCOPE_WRITE')")
    @PostMapping("/admin-message")
    public ResponseEntity<String> getAdminData(@RequestParam("message") String message) {
        return ResponseEntity.ok("Admin::" + " has this message:" + message);
    }

   /* private UserDetails getUserDetails(String userName) {
        var uri = "http://localhost:8585/city-statement/auth/api/user-details/v1/userinfo?emailId="+userName;
        ResponseEntity<UserDetails> userInfo = restTemplate.getForEntity(uri, UserDetails.class);
        return userInfo.getBody();
    }*/

}
