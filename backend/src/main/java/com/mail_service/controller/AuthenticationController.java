package com.mail_service.controller;

import com.mail_service.dto.request.LoginRequest;
import com.mail_service.dto.response.ApiResponse;
import com.mail_service.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/introspect")
    public ApiResponse<?> introspect(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ApiResponse.builder()
                    .status("fail")
                    .message("Invalid or missing Authorization header")
                    .build();
        }
        String token = authorizationHeader.substring(7);
        boolean isValid = authenticationService.introspect(token);
        return ApiResponse.builder()
                .status(isValid ? "success" : "fail")
                .message(isValid ? "Token is valid" : "Invalid token")
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginRequest request) {
        String token = authenticationService.authenticate(request);
        return ApiResponse.success(Map.of("token", token));
    }
}
