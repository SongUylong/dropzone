package com.dropzone.auditservice.service;

import com.dropzone.auditservice.model.AuditRecord;
import com.dropzone.auditservice.repository.AuditRecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final ObjectMapper objectMapper;
    private final AuditRecordRepository auditRecordRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @KafkaListener(topics = {"user-events", "payment-events", "order-events", "inventory-events"}, groupId = "audit-service-group")
    @Transactional
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
                .topic(topic)
                .eventType(eventType)
                .orderNumber(orderNumber)
                .userId(userId)
                .payload(message)
                .formattedMessage(formattedMessage)
                .receivedAt(receivedAt)
                .build();

        auditRecordRepository.save(record);
        log.info("Audit Record persisted to database! Total records: {} [Type: {}]", auditRecordRepository.count(), eventType);
    }

    private String buildFormattedMessage(String eventType, JsonNode node, String userId, String orderNumber) {
        String cleanUserId = (userId != null && !userId.isBlank()) ? userId : "unknown";
        String cleanOrderNum = (orderNumber != null && !orderNumber.isBlank()) ? orderNumber : "unknown";

        return switch (eventType) {
            case "UserRegistered" -> String.format("User %s registered", cleanUserId);
            case "OrderCreated" -> String.format("User %s created Order %s", cleanUserId, cleanOrderNum);
            case "InventoryReserved" -> {
                int qty = node.has("quantity") ? node.get("quantity").asInt() : 0;
                String category = node.has("categoryName") ? node.get("categoryName").asText() : "unknown";
                yield String.format("Inventory reserved %d %s tickets", qty, category);
            }
            case "PaymentCompleted", "PaymentSucceeded" -> "Payment succeeded";
            case "PaymentFailed" -> "Payment failed";
            case "OrderConfirmed" -> "Order confirmed";
            case "OrderCancelled" -> "Order cancelled";
            case "InventoryReleased" -> "Inventory released";
            default -> String.format("%s event processed for Order %s", eventType, cleanOrderNum);
        };
    }

    public List<AuditRecord> getAllAuditRecords() {
        return auditRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "receivedAt"));
    }

    public Page<AuditRecord> getAllAuditRecordsPaged(int page, int size) {
        return auditRecordRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "receivedAt")));
    }

    public List<AuditRecord> getAuditRecordsByTopic(String topic) {
        return auditRecordRepository.findByTopicIgnoreCase(topic);
    }

    public List<AuditRecord> getAuditRecordsByOrderNumber(String orderNumber) {
        return auditRecordRepository.findByOrderNumberIgnoreCase(orderNumber);
    }

    public String getFormattedAuditLog() {
        List<AuditRecord> records = getAllAuditRecords();
        StringBuilder sb = new StringBuilder("AUDIT LOG\n\n");
        for (int i = 0; i < records.size(); i++) {
            AuditRecord record = records.get(i);
            String timeStr = TIME_FORMATTER.format(record.getReceivedAt() != null ? record.getReceivedAt() : Instant.now());
            String msg = record.getFormattedMessage() != null && !record.getFormattedMessage().isBlank()
                    ? record.getFormattedMessage()
                    : record.getEventType();
            sb.append(timeStr).append("\n").append(msg);
            if (i < records.size() - 1) {
                sb.append("\n\n");
            }
        }
        return sb.toString();
    }
}
