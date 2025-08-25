// src/main/java/com/aluminate/aluminate_organization_backend/service/notification/tap/GlobalEventsTap.java
package com.aluminate.aluminate_organization_backend.service.notification.tap;

import com.aluminate.aluminate_organization_backend.config.event.GlobalEvent;
import com.aluminate.aluminate_organization_backend.model.NotificationRequest;
import com.aluminate.aluminate_organization_backend.service.notification.NotificationProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class GlobalEventsTap {

    private static final Logger log = LoggerFactory.getLogger(GlobalEventsTap.class);
    private final NotificationProducer producer;

    public GlobalEventsTap(NotificationProducer producer) {
        this.producer = producer;
    }

    @KafkaListener(
            topics = "global-events",
            containerFactory = "tapListenerFactory"   // uses the dedicated, new factory you added
    )
    public void onGlobalEvent(
            @Payload GlobalEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String orgKey,   // use this as tenant/org identifier
            Acknowledgment ack
    ) {
        try {
            String eventType = event.getEventType(); // this exists in your codebase
            String eventId   = String.valueOf(event.getEventId());

            switch (eventType) {
                // Use the event types that actually arrive on 'global-events'.
                // If you don't have EventPublished/Cancelled here, this will simply no-op (safe).
                case "EventPublished" -> emitEventPublished(orgKey, eventId);
                case "EventCancelled" -> emitEventCancelled(orgKey, eventId);

                // These are examples of types you mentioned earlier; usually you wouldn't notify members for them.
                case "OrganizationUpdated", "AdminUpdated" -> {
                    // No notification needed — leave empty or add admin-only notifications if required.
                }

                default -> log.debug("Tap: ignoring eventType={} id={}", eventType, eventId);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Tap error for type={} id={}", event.getEventType(), event.getEventId(), e);
            throw e; // retry & DLT via tapListenerFactory
        }
    }

    private void emitEventPublished(String tenantOrOrg, String eventId) {
        // Minimal payload (no getPayload() used). You can enrich later by DB lookup.
        producer.send(
                NotificationRequest.of(
                        tenantOrOrg,                          // tenantId from Kafka message key
                        "member-123",                         // TODO: replace with real recipients
                        Set.of("IN_APP", "EMAIL"),
                        "EVENT_PUBLISHED",
                        "event_published",
                        Map.of(
                                "eventId", eventId,
                                // Optional values you can fill later via repository lookup:
                                "eventName", "Event " + eventId,
                                "start",    "TBD",
                                "ctaUrl",   "https://app/events/" + eventId
                        ),
                        "EVENT_PUBLISHED:" + eventId + ":member-123"
                )
        );
    }

    private void emitEventCancelled(String tenantOrOrg, String eventId) {
        producer.send(
                NotificationRequest.of(
                        tenantOrOrg,
                        "member-123",                         // TODO: replace with real recipients
                        Set.of("IN_APP", "EMAIL"),
                        "EVENT_CANCELLED",
                        "event_cancelled",
                        Map.of(
                                "eventId", eventId,
                                "ctaUrl", "https://app/events/" + eventId
                        ),
                        "EVENT_CANCELLED:" + eventId + ":member-123"
                )
        );
    }
}
