package com.aluminate.aluminate_organization_backend.service.notification.worker;

import com.aluminate.aluminate_organization_backend.config.kafka.KafkaTopics;
import com.aluminate.aluminate_organization_backend.model.NotificationRequest;
import com.aluminate.aluminate_organization_backend.service.notification.infra.IdempotencyService;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationConsumer.class);
    private final JavaMailSender mailSender;
    private final IdempotencyService idem;

    @Value("${notifications.email.from:no-reply@aluminate.local}")
    private String from;

    public EmailNotificationConsumer(JavaMailSender mailSender, IdempotencyService idem) {
        this.mailSender = mailSender; this.idem = idem;
    }

    @KafkaListener(topics = KafkaTopics.NOTIF_REQUESTS_V1, containerFactory = "notifListenerFactory")
    public void onRequest(@Payload NotificationRequest req, Acknowledgment ack) {
        try {
            if (req.getChannels() == null || !req.getChannels().contains("EMAIL")) { ack.acknowledge(); return; }
            if (req.getDedupeKey() != null && idem.seen(req.getDedupeKey()+"#email")) { ack.acknowledge(); return; }

            String email = resolveEmail(req.getMemberId()); // TODO: wire to Member repo/service
            if (email == null || email.isBlank()) { ack.acknowledge(); return; }

            var msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(email);
            msg.setSubject(switch (req.getType()) {
                case "EVENT_PUBLISHED" -> "New event: " + req.getData().getOrDefault("eventName","Event");
                case "EVENT_CANCELLED" -> "Event cancelled";
                default -> "Notification";
            });
            msg.setText(buildPlaintext(req));

            mailSender.send(msg);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Email consumer error", e);
            throw new RuntimeException(e);
        }
    }

    private String resolveEmail(String memberId) {
        return "someone@example.com"; // placeholder
    }

    private String buildPlaintext(NotificationRequest req) {
        return switch (req.getType()) {
            case "EVENT_PUBLISHED" -> "Hi! \"" + req.getData().get("eventName")
                    + "\" starts at " + req.getData().get("start")
                    + ". Open: " + req.getData().get("ctaUrl");
            case "EVENT_CANCELLED" -> "Heads up: an event was cancelled. Check: "
                    + req.getData().get("ctaUrl");
            default -> "You have a new notification.";
        };
    }
}
