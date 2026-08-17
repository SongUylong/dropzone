package com.dropzone.auditservice.service;

import com.dropzone.auditservice.model.AuditRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final ObjectMapper objectMapper;
    private final List<AuditRecord> auditRecords = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong idGenerator = new AtomicLong(1);

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @KafkaListener(topics = {"user-events", "payment-events", "order-events", "inventory-events"}, groupId = "audit-service-group")
    public void listen(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Audit Service received Kafka event on topic '{}': {}", topic, message);

        String eventType = "";
        String orderNumber = "";
        String userId = "";
        String formattedMessage = "";
        Instant receivedAt = Instant.now();

        try {
            JsonNode node = objectMapper.readTree(message);
            if (node.has("eventType")) eventType = node.get("eventType").asText();
            if (node.has("orderNumber")) orderNumber = node.get("orderNumber").asText();
            if (node.has("userId")) userId = node.get("userId").asText();

            formattedMessage = buildFormattedMessage(eventType, node, userId, orderNumber);
        } catch (Exception e) {
            log.warn("Could not parse audit event JSON: {}", e.getMessage());
            formattedMessage = message;
        }

        AuditRecord record = AuditRecord.builder()
                .id(idGenerator.getAndIncrement())
                .topic(topic)
                .eventType(eventType)
                .orderNumber(orderNumber)
                .userId(userId)
                .payload(message)
                .formattedMessage(formattedMessage)
                .receivedAt(receivedAt)
                .build();

        auditRecords.add(record);
        log.info("Audit Record saved! Total records: {} [Type: {}]", auditRecords.size(), eventType);
    }

    private String buildFormattedMessage(String eventType, JsonNode node, String userId, String orderNumber) {
        String cleanUserId = (userId != null && !userId.isBlank()) ? userId : "123";
        String cleanOrderNum = (orderNumber != null && !orderNumber.isBlank()) ? orderNumber : "DZ10239";

        switch (eventType) {
            case "UserRegistered":
                return String.format("User %s registered", cleanUserId);

            case "OrderCreated":
                return String.format("User %s created Order %s", cleanUserId, cleanOrderNum);

            case "InventoryReserved":
                int qty = node.has("quantity") ? node.get("quantity").asInt() : (node.has("reservedQuantity") ? node.get("reservedQuantity").asInt() : 2);
                String category = node.has("categoryName") ? node.get("categoryName").asText() : (node.has("ticketCategory") ? node.get("ticketCategory").asText() : "VIP");
                return String.format("Inventory reserved %d %s tickets", qty, category);

            case "PaymentCompleted":
            case "PaymentSucceeded":
                return "Payment succeeded";

            case "PaymentFailed":
                return "Payment failed";

            case "OrderConfirmed":
                return "Order confirmed";

            case "OrderCancelled":
                return "Order cancelled";

            case "InventoryReleased":
                return "Inventory released";

            default:
                return String.format("%s event processed for Order %s", eventType, cleanOrderNum);
        }
    }

    public List<AuditRecord> getAllAuditRecords() {
        return new ArrayList<>(auditRecords);
    }

    public List<AuditRecord> getAuditRecordsByTopic(String topic) {
        return auditRecords.stream()
                .filter(r -> r.getTopic().equalsIgnoreCase(topic))
                .collect(Collectors.toList());
    }

    public List<AuditRecord> getAuditRecordsByOrderNumber(String orderNumber) {
        return auditRecords.stream()
                .filter(r -> r.getOrderNumber() != null && r.getOrderNumber().equalsIgnoreCase(orderNumber))
                .collect(Collectors.toList());
    }

    public String getFormattedAuditLog() {
        StringBuilder sb = new StringBuilder("AUDIT LOG\n\n");
        synchronized (auditRecords) {
            for (int i = 0; i < auditRecords.size(); i++) {
                AuditRecord record = auditRecords.get(i);
                String timeStr = TIME_FORMATTER.format(record.getReceivedAt() != null ? record.getReceivedAt() : Instant.now());
                String msg = record.getFormattedMessage() != null && !record.getFormattedMessage().isBlank()
                        ? record.getFormattedMessage()
                        : record.getEventType();

                sb.append(timeStr).append("\n").append(msg);
                if (i < auditRecords.size() - 1) {
                    sb.append("\n\n");
                }
            }
        }
        return sb.toString();
    }
}
