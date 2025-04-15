package com.echo_english.controller;

import com.echo_english.dto.request.ResetPasswordRequest;
import com.echo_english.entity.User;
import com.echo_english.service.MailService;
import com.echo_english.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private MailService mailService;
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        try {
            user.setActive(false);

            if (userService.create(user)) {
                mailService.generateAndSendOtp(user.getEmail());
                return ResponseEntity.ok("Registration in progress. Please verify your email.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to register user.");
            }
        } catch (Exception e) {
            log.error("Unexpected error during registration for email {}: {}", user.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred during registration.");
        }
    }

    @PostMapping("/validate-otp-register")
    public ResponseEntity<String> validateOtpRegister(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String otpCode = requestBody.get("code");

        if (email == null || otpCode == null) {
            return ResponseEntity.badRequest().body("Email and OTP code are required.");
        }

        if (mailService.validateOtp(email, otpCode)) {
            userService.activateUser(email);
            return ResponseEntity.ok("Email verified successfully. Registration completed.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null) {
            return ResponseEntity.badRequest().body("Email is required.");
        }

        if (!userService.isEmailExists(email)) {
            return ResponseEntity.badRequest().body("Mail not found");
        }

        mailService.generateAndSendOtp(email);
        return ResponseEntity.ok("OTP has been send to your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getEmail() == null || request.getOtpCode() == null || request.getNewPassword() == null) {
            return ResponseEntity.badRequest().body("Email, OTP, and new password are required.");
        }

        boolean isValid = mailService.validateOtp(request.getEmail(), request.getOtpCode());
        if (!isValid) {
            return ResponseEntity.badRequest().body("OTP invalid.");
        }

        userService.updatePassword(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok("Reset password successfully");
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
