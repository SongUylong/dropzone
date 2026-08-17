package com.dropzone.orderservice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public static final String TOPIC_ORDER_EVENTS = "order-events";

    public void sendOrderEvent(OrderEvent event) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            log.info("Producing Kafka event [{}] to topic '{}': {}", event.getEventType(), TOPIC_ORDER_EVENTS, jsonPayload);
            kafkaTemplate.send(TOPIC_ORDER_EVENTS, event.getOrderNumber(), jsonPayload);
        } catch (Exception e) {
            log.error("Failed to produce Kafka order event for order {}: {}", event.getOrderNumber(), e.getMessage(), e);
        }
    }
}
