package com.aluminate.aluminate_organization_backend.chat.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.aluminate.aluminate_organization_backend.chat.model.Message;

public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findByOrgIdAndGroupIdAndCreatedAtLessThanEqualOrderByCreatedAtDesc(
            String orgId, String groupId, Instant createdAt, Pageable pageable);

    List<Message> findByOrgIdAndGroupIdOrderByCreatedAtDesc(
            String orgId, String groupId, Pageable pageable);
}
