package com.dropzone.eventservice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public static final String TOPIC_EVENT_EVENTS = "event-events";

    public void sendEventEvent(EventEvent event) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            log.info("Producing Kafka event [{}] to topic '{}': {}", event.getEventType(), TOPIC_EVENT_EVENTS, jsonPayload);
            kafkaTemplate.send(TOPIC_EVENT_EVENTS, String.valueOf(event.getId()), jsonPayload);
        } catch (Exception e) {
            log.error("Failed to produce Kafka event for event {}: {}", event.getId(), e.getMessage(), e);
        }
    }
}
