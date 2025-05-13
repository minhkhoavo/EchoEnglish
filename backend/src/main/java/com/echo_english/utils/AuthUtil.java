package com.echo_english.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

public class AuthUtil {
    private static Jwt getJwt() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof Jwt jwt) {
            return jwt;
        }

        throw new IllegalStateException("Not found JWT!");
    }

    public static Long getUserId() {
        String userIdStr = getJwt().getClaimAsString("sub");
        return Long.parseLong(userIdStr);
    }

    public static List<String> getRoles() {
        return getJwt().getClaimAsStringList("roles");
    }

    public static boolean hasRole(String role) {
        List<String> roles = getRoles();
        return roles != null && roles.contains(role);
    }
}