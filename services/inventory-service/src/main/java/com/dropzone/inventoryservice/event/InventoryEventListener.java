package com.dropzone.inventoryservice.event;

import com.dropzone.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"payment-events", "order-events"}, groupId = "inventory-service-confirmation-group")
    public void handlePaymentAndOrderEvents(String message) {
        log.info("[Inventory Event Listener] Received event: {}", message);
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.has("eventType") ? node.get("eventType").asText() : "";
            String reservationId = node.has("reservationId") ? node.get("reservationId").asText() : null;

            if (reservationId == null || reservationId.isBlank()) {
                log.debug("[Inventory Event Listener] Event {} had no reservationId. Skipping.", eventType);
                return;
            }

            switch (eventType) {
                case "PaymentCompleted", "PaymentSucceeded", "OrderConfirmed" -> {
                    log.info("[Inventory Event Listener] Confirming inventory reservation: {}", reservationId);
                    try {
                        inventoryService.confirmReservation(reservationId);
                    } catch (Exception e) {
                        log.warn("[Inventory Event Listener] Reservation {} already confirmed or expired: {}", reservationId, e.getMessage());
                    }
                }
                case "PaymentFailed", "OrderCancelled" -> {
                    log.info("[Inventory Event Listener] Releasing inventory reservation: {}", reservationId);
                    try {
                        inventoryService.cancelReservation(reservationId);
                    } catch (Exception e) {
                        log.warn("[Inventory Event Listener] Reservation {} already cancelled or expired: {}", reservationId, e.getMessage());
                    }
                }
                default -> log.debug("[Inventory Event Listener] Unhandled eventType: {}", eventType);
            }
        } catch (Exception e) {
            log.error("[Inventory Event Listener] Error processing Kafka event: {}", e.getMessage(), e);
        }
    }
}
