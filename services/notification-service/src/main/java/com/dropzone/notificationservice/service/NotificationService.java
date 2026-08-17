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

    @KafkaListener(id = "notif-order-listener", topics = "order-events", groupId = "notification-group-v2")
    public void listenOrderEvents(String message) {
        handleEvent(message, "order-events");
    }

    @KafkaListener(id = "notif-payment-listener", topics = "payment-events", groupId = "notification-group-v2")
    public void listenPaymentEvents(String message) {
        handleEvent(message, "payment-events");
    }

    @KafkaListener(id = "notif-inventory-listener", topics = "inventory-events", groupId = "notification-group-v2")
    public void listenInventoryEvents(String message) {
        handleEvent(message, "inventory-events");
    }

    public void handleEvent(String message, String topic) {
        log.info("Notification Service received Kafka event on topic '{}': {}", topic, message);

        String eventType = "";
        String orderNumber = "";
        String userId = "";
        Double amount = null;

        try {
            JsonNode node = objectMapper.readTree(message);
            if (node.has("eventType")) eventType = node.get("eventType").asText();
            if (node.has("orderNumber")) orderNumber = node.get("orderNumber").asText();
            if (node.has("userId")) userId = node.get("userId").asText();
            if (node.has("amount") && !node.get("amount").isNull()) amount = node.get("amount").asDouble();
            else if (node.has("totalAmount") && !node.get("totalAmount").isNull()) amount = node.get("totalAmount").asDouble();
        } catch (Exception ignored) {
        }

        String msgText;
        if ("OrderConfirmed".equalsIgnoreCase(eventType)) {
            msgText = String.format("Order %s confirmed for User %s", orderNumber, userId);
        } else if ("OrderCreated".equalsIgnoreCase(eventType)) {
            msgText = String.format("Order %s created for User %s", orderNumber, userId);
        } else if ("PaymentCompleted".equalsIgnoreCase(eventType)) {
            msgText = String.format("Payment completed for Order %s (Amount: $%.2f)", orderNumber, amount != null ? amount : 0.0);
        } else if ("PaymentFailed".equalsIgnoreCase(eventType)) {
            msgText = String.format("Payment failed for Order %s", orderNumber);
        } else if ("InventoryReserved".equalsIgnoreCase(eventType)) {
            msgText = String.format("Inventory reserved for User %s", userId);
        } else if ("InventoryReleased".equalsIgnoreCase(eventType)) {
            msgText = String.format("Inventory released for User %s", userId);
        } else {
            msgText = String.format("Notification sent to User %s for Order %s: [%s]", userId, orderNumber, eventType);
        }

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

    public List<NotificationRecord> getNotificationsByUserId(String userId) {
        return notifications.stream()
                .filter(n -> n.getUserId() != null && n.getUserId().equalsIgnoreCase(userId))
                .collect(Collectors.toList());
    }
}
