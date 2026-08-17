package com.dropzone.notificationservice.service;

import com.dropzone.notificationservice.model.NotificationRecord;
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
public class NotificationService {

    private final ObjectMapper objectMapper;
    private final List<NotificationRecord> notifications = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong idGenerator = new AtomicLong(1);

    @KafkaListener(topics = {"payment-events", "order-events"}, groupId = "notification-service-group")
    public void listen(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Notification Service received Kafka event on topic '{}': {}", topic, message);

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

        String msgText = String.format("Notification sent to User %s for Order %s: [%s]", userId, orderNumber, eventType);

        NotificationRecord record = NotificationRecord.builder()
                .id(idGenerator.getAndIncrement())
                .topic(topic)
                .eventType(eventType)
                .orderNumber(orderNumber)
                .userId(userId)
                .messageText(msgText)
                .sentAt(Instant.now())
                .build();

        notifications.add(record);
        log.info("Notification recorded: {}", msgText);
    }

    public List<NotificationRecord> getAllNotifications() {
        return new ArrayList<>(notifications);
    }

    public List<NotificationRecord> getNotificationsByOrderNumber(String orderNumber) {
        return notifications.stream()
                .filter(n -> n.getOrderNumber() != null && n.getOrderNumber().equalsIgnoreCase(orderNumber))
                .collect(Collectors.toList());
    }
}
