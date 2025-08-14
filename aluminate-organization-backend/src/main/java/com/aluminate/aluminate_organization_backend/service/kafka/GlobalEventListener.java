package com.aluminate.aluminate_organization_backend.service.kafka;

import com.aluminate.aluminate_organization_backend.config.event.GlobalEvent;
import com.aluminate.aluminate_organization_backend.exception.NonRetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class GlobalEventListener {

    private final Logger logger = LoggerFactory.getLogger(GlobalEventListener.class);

    @KafkaListener(topics = "global-events")
    public void consume(
            @Payload GlobalEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String organizationName,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            logger.info("Processing event {} for organization {}", event.getEventId(), organizationName);

            // Your business logic here
            processEvent(event);

            // Manually commit offset
            acknowledgment.acknowledge();
            logger.info("Successfully processed event {}", event.getEventId());

        } catch (NonRetryableException e) {
            logger.error("Non-retryable error for event {}", event.getEventId(), e);
            acknowledgment.acknowledge(); // Skip this message
        } catch (Exception e) {
            logger.error("Error processing event {}", event.getEventId(), e);
            throw e; // Will trigger retry mechanism
        }
    }

    private void processEvent(GlobalEvent event) {
        // Implement your processing logic
        switch(event.getEventType()) {
            case "OrganizationUpdated":
                // Handle organization update
                break;
            case "AdminUpdated":
                // Handle admin update
                break;
            default:
                logger.warn("Unknown event type: {}", event.getEventType());
        }
    }
}
