package com.aluminate.aluminate_organization_backend.service.notification.worker;

import com.aluminate.aluminate_organization_backend.config.kafka.KafkaTopics;
import com.aluminate.aluminate_organization_backend.model.InAppNotification;
import com.aluminate.aluminate_organization_backend.model.NotificationRequest;
import com.aluminate.aluminate_organization_backend.repository.InAppNotificationRepository;
import com.aluminate.aluminate_organization_backend.service.notification.infra.IdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class InAppNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationConsumer.class);

    private final InAppNotificationRepository repo;
    private final IdempotencyService idem;
    private final ObjectMapper mapper = new ObjectMapper();

    public InAppNotificationConsumer(InAppNotificationRepository repo, IdempotencyService idem) {
        this.repo = repo;
        this.idem = idem;
    }

    @KafkaListener(
            topics = KafkaTopics.NOTIF_REQUESTS_V1,
            containerFactory = "notifListenerFactory",
            groupId = "notif-inapp-workers",   // <-- own group so in-app gets EVERY message
            id = "notif-inapp-1"               // <-- stable listener id (nice for logs/ops)
    )
    public void onRequest(@Payload NotificationRequest req, Acknowledgment ack) {
        try {
            if (req == null) {
                log.warn("In-app: received null payload; ack and skip");
                ack.acknowledge();
                return;
            }

            // Only handle IN_APP channel
            if (req.getChannels() == null || !req.getChannels().contains("IN_APP")) {
                ack.acknowledge();
                return;
            }

            // Idempotency (skip if we've seen the dedupe key)
            if (req.getDedupeKey() != null && idem.seen(req.getDedupeKey())) {
                log.debug("In-app: dedupe hit for key={}", req.getDedupeKey());
                ack.acknowledge();
                return;
            }

            // Persist notification
            var entity = InAppNotification.builder()
                    .memberId(req.getMemberId())
                    .tenantId(req.getTenantId())
                    .type(req.getType())
                    .payloadJson(mapper.writeValueAsString(req.getData()))
                    .createdAt(java.time.Instant.now()) // ensure NOT NULL
                    .build();

            repo.save(entity);

            log.info("In-app: saved notification id={} tenant={} member={} type={}",
                    entity.getId(), entity.getTenantId(), entity.getMemberId(), entity.getType());

            // Commit the offset
            ack.acknowledge();

        } catch (Exception e) {
            log.error("In-app consumer error", e);
            // Re-throw so the container error handler (retries/DLT) can kick in
            throw new RuntimeException(e);
        }
    }
}
