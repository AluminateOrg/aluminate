package com.aluminate.aluminate_organization_backend.chat.security;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import lombok.Value;

@Value
public class JwtPrincipal implements Principal {
    String userId;
    String orgId;
    Set<String> roles;

    @Override
    public String getName() {
        return userId;
    }

    public boolean isAdmin() {
        return roles != null && roles.contains("ADMIN");
    }

    public static JwtPrincipal of(String userId, String orgId, Set<String> roles) {
        return new JwtPrincipal(userId, orgId, roles == null ? Collections.emptySet() : roles);
    }
}
