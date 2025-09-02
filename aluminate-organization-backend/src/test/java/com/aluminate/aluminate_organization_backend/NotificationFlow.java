package com.aluminate.aluminate_organization_backend;

import com.aluminate.aluminate_organization_backend.model.NotificationRequest;
import com.aluminate.aluminate_organization_backend.repository.InAppNotificationRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.*;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 6,
        topics = { "notification.requests.v1", "notification.requests.v1.DLT" }
)
class NotificationFlowIT {

    @Autowired MockMvc mvc;
    @Autowired InAppNotificationRepository repo;

    @Value("${spring.kafka.bootstrap-servers}")
    String bootstrapServers;

    @MockBean JavaMailSender mailSender;

    // Wait for @KafkaListener containers so we don't race
    @Autowired EmbeddedKafkaBroker broker;
    @Autowired KafkaListenerEndpointRegistry registry;

    private KafkaTemplate<String, NotificationRequest> notifProducer;

    @BeforeEach
    void setUp() throws Exception {
        // prevent real emails during tests
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Make sure type headers are included (belt-and-suspenders)
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        notifProducer = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));

        // Ensure all @KafkaListener containers are assigned before we produce
        registry.getListenerContainers().forEach(container ->
                ContainerTestUtils.waitForAssignment(container, broker.getPartitionsPerTopic())
        );
    }

    @Test
    void endToEnd_workerOnly() throws Exception {
        String eid = "evt-it-" + UUID.randomUUID().toString().substring(0, 8);

        NotificationRequest req = new NotificationRequest();
        req.setMessageId(UUID.randomUUID().toString());
        req.setTenantId("org-demo");
        req.setMemberId("member-123");
        req.setChannels(new LinkedHashSet<>(List.of("IN_APP", "EMAIL"))); // Set, not List
        req.setType("EVENT_PUBLISHED");
        req.setTemplate("event_published");

        Map<String, Object> data = new HashMap<>();
        data.put("eventId", eid);
        data.put("eventName", "Integration Test Event");
        data.put("start", "TBD");
        data.put("ctaUrl", "https://app/events/" + eid);
        req.setData(data);

        req.setDedupeKey("EVENT_PUBLISHED:" + eid + ":member-123");
        req.setCreatedAt(java.time.Instant.now());    // ✅


        // Produce directly to the worker topic
        notifProducer.send("notification.requests.v1", "member-123", req).get();

        // Wait for the consumer to persist it
        Awaitility.await()
                .atMost(Duration.ofSeconds(45))
                .pollInterval(Duration.ofMillis(200))
                .until(() -> repo.count() > 0);

        // Unread count endpoint OK
        mvc.perform(get("/api/v1/portal/notifications/{memberId}/unread-count", "member-123"))
                .andExpect(status().isOk());

        // List contains our row
        mvc.perform(get("/api/v1/portal/notifications/{memberId}", "member-123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"memberId\":\"member-123\"")))
                .andExpect(content().string(containsString("\"tenantId\":\"org-demo\"")))
                .andExpect(content().string(containsString("\"type\":\"EVENT_PUBLISHED\"")));

        // Mark read → unread count becomes 0
        var first = repo.findAll().get(0);
        mvc.perform(post("/api/v1/portal/notifications/{id}/read", first.getId()))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/portal/notifications/{memberId}/unread-count", "member-123"))
                .andExpect(status().isOk())
                .andExpect(content().string(is("0")));
    }
}
