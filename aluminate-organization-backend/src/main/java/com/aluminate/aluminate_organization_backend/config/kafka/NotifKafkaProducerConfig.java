package com.aluminate.aluminate_organization_backend.config.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

import static com.aluminate.aluminate_organization_backend.config.kafka.KafkaProducerConfig.getStringObjectProducerFactory;

@Configuration
public class NotifKafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrap;

    @Bean("notifProducerFactory")
    public ProducerFactory<String, Object> notifProducerFactory() {
        return getStringObjectProducerFactory(bootstrap);
    }

    @Bean("notifKafkaTemplate")
    public KafkaTemplate<String, Object> notifKafkaTemplate(
            @Qualifier("notifProducerFactory") ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf);
    }
}
