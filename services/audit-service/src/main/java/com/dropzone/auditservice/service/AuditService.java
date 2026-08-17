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

    @KafkaListener(topics = {"payment-events", "order-events", "inventory-events"}, groupId = "audit-service-group")
    public void listen(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Audit Service received Kafka event on topic '{}': {}", topic, message);

        String eventType = "";
        String orderNumber = "";
        String userId = "";

        try {
            JsonNode node = objectMapper.readTree(message);
            if (node.has("eventType")) eventType = node.get("eventType").asText();
            if (node.has("orderNumber")) orderNumber = node.get("orderNumber").asText();
            if (node.has("userId")) userId = node.get("userId").asText();
        } catch (Exception ignored) {
        }

        AuditRecord record = AuditRecord.builder()
                .id(idGenerator.getAndIncrement())
                .topic(topic)
                .eventType(eventType)
                .orderNumber(orderNumber)
                .userId(userId)
                .payload(message)
                .receivedAt(Instant.now())
                .build();

        auditRecords.add(record);
        log.info("Audit Record saved! Total records: {}", auditRecords.size());
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
}
