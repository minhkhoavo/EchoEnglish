package com.mail_service.controller;

import java.util.Map;

import com.mail_service.dto.request.ResetPasswordRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mail_service.entity.User;
import com.mail_service.service.MailService;
import com.mail_service.service.UserService;

@RestController
public class UserController {
    @Autowired
    private MailService mailService;
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody User user) {
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
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/validate-otp-register")
    public ResponseEntity<?> validateOtpRegister(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String otpCode = requestBody.get("code");

        if (mailService.validateOtp(email, otpCode)) {
            userService.activateUser(email);
            return ResponseEntity.ok("Email verified successfully. Registration completed.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (!userService.isEmailExists(email)) {
            return ResponseEntity.badRequest().body("Mail not found");
        }

        generateAndSendOtp(email);
        return ResponseEntity.ok("OTP has been send to your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        boolean isValid = mailService.validateOtp(request.getEmail(), request.getOtpCode());
        if (!isValid) {
            return ResponseEntity.badRequest().body("OTP invalid.");
        }

        userService.updatePassword(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok("Reset password successfully");
    }


    @GetMapping("/test-mail") // Only for test
    public ResponseEntity<?> generateAndSendOtp(@RequestParam(value = "mail") String userMail) {
        return ResponseEntity.ok(mailService.generateAndSendOtp(userMail));
    }
    
    @PostMapping("/validate-otp")
    public ResponseEntity<?> validateOtp(@RequestBody Map<String, String> requestBody) {
        String userId = requestBody.get("userId");
        String otpCode = requestBody.get("code");
        System.out.println("otpCode:::" + otpCode + "/" + userId);
        boolean isValid = mailService.validateOtp(userId, otpCode);

        return isValid
                ? ResponseEntity.ok("OTP is valid")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
    }
}
