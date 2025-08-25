package com.aluminate.aluminate_organization_backend.service.notification.worker;

import com.aluminate.aluminate_organization_backend.config.kafka.KafkaTopics;
import com.aluminate.aluminate_organization_backend.model.InAppNotification;
import com.aluminate.aluminate_organization_backend.model.NotificationRequest;
import com.aluminate.aluminate_organization_backend.repository.InAppNotificationRepository;
import com.aluminate.aluminate_organization_backend.service.notification.infra.IdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
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
        this.repo = repo; this.idem = idem;
    }

    @KafkaListener(topics = KafkaTopics.NOTIF_REQUESTS_V1, containerFactory = "notifListenerFactory")
    public void onRequest(@Payload NotificationRequest req, Acknowledgment ack) {
        try {
            if (req.getChannels() == null || !req.getChannels().contains("IN_APP")) { ack.acknowledge(); return; }
            if (req.getDedupeKey() != null && idem.seen(req.getDedupeKey())) { ack.acknowledge(); return; }

            var entity = InAppNotification.builder()
                    .memberId(req.getMemberId())
                    .tenantId(req.getTenantId())
                    .type(req.getType())
                    .payloadJson(mapper.writeValueAsString(req.getData()))
                    .createdAt(java.time.Instant.now())
                    .build();

            repo.save(entity);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("In-app consumer error", e);
            throw new RuntimeException(e);
        }
    }
}
