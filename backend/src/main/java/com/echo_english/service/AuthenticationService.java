package com.echo_english.service;

import com.echo_english.dto.request.LoginRequest;
import com.echo_english.entity.User;
import com.echo_english.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class AuthenticationService {
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @Autowired
    private UserRepository userRepository;

    public boolean introspect(String token) {
        boolean isValid = true;
        try {
            verifyToken(token);
        } catch (Exception e) {
            isValid = false;
        }

        return isValid;
    }

    public String authenticate(LoginRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not existed"));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new RuntimeException("Invalid password");

        return generateToken(user);
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256); // Header: Chứa loại thuật toán mã hóa

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("mobile-dev")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject()); // Payload: Chứa dữ liệu người dùng như userId, role, exp (thời gian hết hạn),...

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes())); // Signature: Được tạo bằng cách mã hóa Header + Payload với secret key (SIGNER_KEY)
            return jwsObject.serialize(); // serialize() chuyển đổi đối tượng JWSObject thành chuỗi JWT hoàn chỉnh có dạng Header.Payload.Signature
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date())))
            throw new RuntimeException("Token expired");

        return signedJWT;
    }
}
