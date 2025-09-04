package com.aluminate.aluminate_organization_backend.chat.integration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import static org.hibernate.type.descriptor.java.CoercionHelper.toLong;

/**
 * Default implementation using JDBC queries against Postgres.
 * SQL can be adjusted via properties; assumes existence of tables:
 * - group_memberships(org_id, group_id, user_id)
 * - organization_admins(org_id, user_id)
 */
@Component
public class JdbcMembershipClient implements MembershipClient {

    private final JdbcTemplate jdbcTemplate;
    private final String isMemberSql;
    private final String isAdminSql;
    private final String groupsSql;

    public JdbcMembershipClient(
            JdbcTemplate jdbcTemplate,
            @Value("${chat.sql.isMember:select exists(select 1 from member_group where group_id=? and member_id=?)}") String isMemberSql,
            @Value("${chat.sql.isAdmin:select exists(select 1 from organization where id=? and admin_id=?)}") String isAdminSql,
            @Value("${chat.sql.groupsForUser:select group_id from member_group where member_id=? and request_status='APPROVED'}") String groupsSql) {
        this.jdbcTemplate = jdbcTemplate;
        this.isMemberSql = isMemberSql;
        this.isAdminSql = isAdminSql;
        this.groupsSql = groupsSql;
    }

    @Override
    public boolean isMember(String groupId, String userId) {
        Long gId = toLong(groupId, "groupId");
        Long uId = toLong(userId, "userId");
        Boolean exists = jdbcTemplate.queryForObject(isMemberSql, Boolean.class, gId, uId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean isAdmin(String orgId, String userId) {
        Long oId = toLong(orgId, "orgId");
        Long uId = toLong(userId, "userId");
        Boolean exists = jdbcTemplate.queryForObject(isAdminSql, Boolean.class, oId, uId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public Set<String> groupsForUser(String userId) {
        Long uId = toLong(userId, "userId");
        // Query as Long to match DB type, then convert to String for the service contract
        List<Long> list = jdbcTemplate.queryForList(groupsSql, Long.class, uId);
        return new HashSet<>(list.stream().map(String::valueOf).collect(Collectors.toSet()));
    }

    private Long toLong(String value, String name) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid numeric value for " + name + ": " + value);
        }
    }

}
