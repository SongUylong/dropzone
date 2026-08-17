package com.dropzone.userservice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public static final String TOPIC_USER_EVENTS = "user-events";

    public void sendUserEvent(UserEvent event) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            log.info("Producing Kafka event [{}] to topic '{}': {}", event.getEventType(), TOPIC_USER_EVENTS, jsonPayload);
            kafkaTemplate.send(TOPIC_USER_EVENTS, String.valueOf(event.getUserId()), jsonPayload);
        } catch (Exception e) {
            log.error("Failed to produce Kafka user event for user {}: {}", event.getUserId(), e.getMessage(), e);
        }
    }
}
