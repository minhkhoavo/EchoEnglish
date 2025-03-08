package com.mail_service.service;

import com.mail_service.entity.Otp;
import com.mail_service.repository.OtpRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private OtpRepository otpRepository;

    public String generateAndSendOtp(String userMail) {
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        Otp otp = Otp.builder()
                .code(otpCode)
                .userId(userMail)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .build();
        otpRepository.save(otp);

        sendOtpEmail(userMail, otpCode);

        return otpCode;
    }

    public boolean validateOtp(String userId, String otpCode) {
        Otp otp = otpRepository.findByUserIdAndCode(userId, otpCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP"));

        if (otp.getIsUsed() || LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            return false;
        }

        otp.setIsUsed(true);
        otpRepository.save(otp);

        return true;
    }

    public void sendOtpEmail(String recipientEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("admin@mkhoavo.space");
            helper.setTo(recipientEmail);
            helper.setSubject("Your OTP Code");

            String emailContent = "<div style=\"font-family: Helvetica,Arial,sans-serif;overflow:auto;line-height:2\">" +
                    "<div style=\"margin:50px 50px;width:70%;padding:20px 0\">" +
                    "<div style=\"border-bottom:1px solid #eee\">" +
                    "<a href=\"\" style=\"font-size:1.4em;color: #00466a;text-decoration:none;font-weight:600\">Your Brand</a>" +
                    "</div>" +
                    "<p style=\"font-size:1.1em\">Hi,</p>" +
                    "<p>Thank you for choosing Your Brand. Use the following OTP to complete your Sign Up procedures. OTP is valid for 15 minutes</p>" +
                    "<h2 style=\"background: #00466a;margin: 0 auto;width: max-content;padding: 0 10px;color: #fff;border-radius: 4px;\">" + otpCode + "</h2>" +
                    "<p style=\"font-size:0.9em;\">Regards,<br />Your Brand</p>" +
                    "<hr style=\"border:none;border-top:1px solid #eee\" />" +
                    "<div style=\"float:right;padding:8px 0;color:#aaa;font-size:0.8em;line-height:1;font-weight:300\">" +
                    "<p>Your Brand Inc</p>" +
                    "<p>1600 Amphitheatre Parkway</p>" +
                    "<p>California</p>" +
                    "</div>" +
                    "</div>" +
                    "</div>";

            helper.setText(emailContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}
