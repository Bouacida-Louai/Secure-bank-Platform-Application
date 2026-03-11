package com.securebank.common;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UserContext {

    public static String getCurrentKeycloakId() {
        Jwt jwt = getJwt();
        return jwt.getSubject();
    }

    public static String getCurrentEmail() {
        Jwt jwt = getJwt();
        return jwt.getClaimAsString("email");
    }

    public static String getCurrentFullName() {
        Jwt jwt = getJwt();
        String firstName = jwt.getClaimAsString("given_name") != null
                ? jwt.getClaimAsString("given_name") : "";
        String lastName = jwt.getClaimAsString("family_name") != null
                ? jwt.getClaimAsString("family_name") : "";
        return (firstName + " " + lastName).trim();
    }

    public static String getCurrentRole() {
        Jwt jwt = getJwt();

        // manually extract nested claim
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess == null) return "CUSTOMER";

        List<String> roles = (List<String>) realmAccess.get("roles");

        if (roles == null) return "CUSTOMER";

        return roles.stream()
                .filter(r -> !r.startsWith("default")
                        && !r.equals("offline_access")
                        && !r.equals("uma_authorization"))
                .findFirst()
                .orElse("CUSTOMER");
    }

    public static String getBranchId() {
        Jwt jwt = getJwt();
        return jwt.getClaimAsString("branch_id");
    }

    private static Jwt getJwt() {
        return (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
