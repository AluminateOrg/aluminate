package com.aluminate.aluminate_organization_backend.config.kafka;

import com.aluminate.aluminate_organization_backend.model.NotificationRequest;
import com.aluminate.aluminate_organization_backend.config.event.GlobalEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class NotifKafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrap;

    /* ----- EVENT TAP: reads 'global-events' (own group; won't touch existing listener) ----- */
    @Bean("tapConsumerFactory")
    public ConsumerFactory<String, Object> tapConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        // Force payload into our GlobalEvent type
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, GlobalEvent.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "organization-service-notif-tap");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean("tapListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> tapListenerFactory(
            @Qualifier("tapConsumerFactory") ConsumerFactory<String, Object> cf,
            @Qualifier("notifKafkaTemplate") KafkaTemplate<String, Object> template) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(cf);

        var recoverer = new DeadLetterPublishingRecoverer(template,
                (rec, ex) -> new TopicPartition(rec.topic() + ".DLT", rec.partition()));
        var err = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
        factory.setCommonErrorHandler(err);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    /* ----- NOTIF WORKERS: read 'notification.requests.v1' (their own group) ----- */
    @Bean("notifConsumerFactory")
    public ConsumerFactory<String, Object> notifConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NotificationRequest.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notif-workers");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean("notifListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> notifListenerFactory(
            @Qualifier("notifConsumerFactory") ConsumerFactory<String, Object> cf,
            @Qualifier("notifKafkaTemplate") KafkaTemplate<String, Object> template) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(cf);

        var recoverer = new DeadLetterPublishingRecoverer(template,
                (rec, ex) -> new TopicPartition(KafkaTopics.NOTIF_REQUESTS_DLT, rec.partition()));
        var err = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 5));
        factory.setCommonErrorHandler(err);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
