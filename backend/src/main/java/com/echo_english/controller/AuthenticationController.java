package com.echo_english.controller;

import com.echo_english.dto.request.LoginRequest;
import com.echo_english.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class); // Logger

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/introspect")
    public ResponseEntity<Map<String, String>> introspect(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing Authorization header"));
        }
        String token = authorizationHeader.substring(7); // Cắt bỏ chuỗi "Bearer " để lấy token JWT.
        boolean isValid = authenticationService.introspect(token);

        if (isValid) {
            return ResponseEntity.ok(Map.of("message", "Token is valid"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid token"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = authenticationService.authenticate(request); // Assumes this throws exception on failure
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) { // Replace with more specific AuthenticationException if available
            log.warn("Authentication failed for email {}: {}", request.getEmail(), e.getMessage());
            // Return a generic message for security reasons
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password."));
        }
    }
}