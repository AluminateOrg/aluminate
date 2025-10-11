package com.aluminate.aluminate_organization_backend.chat.integration;

import java.util.Set;

/**
 * Interface to check membership and roles against authoritative Postgres.
 * Implementations may use JPA/JdbcTemplate or call external services.
 */
public interface MembershipClient {
    boolean isMember(String groupId, String userId);

    boolean isAdmin(String orgId, String userId);

    Set<String> groupsForUser(String userId);
}
