package com.dropzone.inventoryservice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public static final String TOPIC_INVENTORY_EVENTS = "inventory-events";

    public void sendInventoryEvent(InventoryEvent event) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            log.info("Producing Kafka event [{}] to topic '{}': {}", event.getEventType(), TOPIC_INVENTORY_EVENTS, jsonPayload);
            kafkaTemplate.send(TOPIC_INVENTORY_EVENTS, event.getReservationId() != null ? event.getReservationId() : event.getTicketCategoryId().toString(), jsonPayload);
        } catch (Exception e) {
            log.error("Failed to produce Kafka inventory event: {}", e.getMessage(), e);
        }
    }
}
