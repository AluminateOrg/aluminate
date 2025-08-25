// src/main/java/com/aluminate/aluminate_organization_backend/service/notification/NotificationProducer.java
package com.aluminate.aluminate_organization_backend.service.notification;

import com.aluminate.aluminate_organization_backend.config.kafka.KafkaTopics;
import com.aluminate.aluminate_organization_backend.model.NotificationRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private final KafkaTemplate<String, Object> template;

    public NotificationProducer(
            @Qualifier("notifKafkaTemplate") KafkaTemplate<String, Object> template
    ) {
        this.template = template;
    }

    public void send(NotificationRequest req) {
        template.send(KafkaTopics.NOTIF_REQUESTS_V1, req.getMemberId(), req);
    }
}
