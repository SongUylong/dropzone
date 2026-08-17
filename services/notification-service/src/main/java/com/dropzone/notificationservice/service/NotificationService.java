package com.dropzone.notificationservice.service;

import com.dropzone.notificationservice.model.NotificationRecord;
import com.dropzone.notificationservice.repository.NotificationRecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ObjectMapper objectMapper;
    private final JobService jobService;
    private final NotificationRecordRepository notificationRecordRepository;

    @KafkaListener(id = "notif-order-listener", topics = "order-events", groupId = "notification-service-jobs-group")
    public void listenOrderEvents(String message) {
        handleEvent(message, "order-events");
    }

    @KafkaListener(id = "notif-payment-listener", topics = "payment-events", groupId = "notification-service-jobs-group")
    public void listenPaymentEvents(String message) {
        handleEvent(message, "payment-events");
    }

    @KafkaListener(id = "notif-inventory-listener", topics = "inventory-events", groupId = "notification-service-jobs-group")
    public void listenInventoryEvents(String message) {
        handleEvent(message, "inventory-events");
    }

    @Transactional
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
        } catch (Exception e) {
            log.warn("Failed to parse Kafka message JSON: {}", e.getMessage());
        }

        String msgText = buildMessageText(eventType, orderNumber, userId, amount);

        NotificationRecord record = NotificationRecord.builder()
                .topic(topic)
                .eventType(eventType)
                .orderNumber(orderNumber)
                .userId(userId)
                .messageText(msgText)
                .sentAt(Instant.now())
                .build();

        notificationRecordRepository.save(record);
        log.info("Notification persisted to database: {}", msgText);

        // If event is OrderConfirmed, dispatch worker jobs to RabbitMQ queues
        if ("OrderConfirmed".equalsIgnoreCase(eventType)) {
            jobService.dispatchJobsForOrderConfirmed(orderNumber, userId);
        }
    }

    private String buildMessageText(String eventType, String orderNumber, String userId, Double amount) {
        return switch (eventType) {
            case "OrderConfirmed" -> String.format("Order %s confirmed for User %s", orderNumber, userId);
            case "OrderCreated" -> String.format("Order %s created for User %s", orderNumber, userId);
            case "PaymentCompleted" -> String.format("Payment completed for Order %s (Amount: $%.2f)", orderNumber, amount != null ? amount : 0.0);
            case "PaymentFailed" -> String.format("Payment failed for Order %s", orderNumber);
            case "InventoryReserved" -> String.format("Inventory reserved for User %s", userId);
            case "InventoryReleased" -> String.format("Inventory released for User %s", userId);
            default -> String.format("Notification sent to User %s for Order %s: [%s]", userId, orderNumber, eventType);
        };
    }

    public List<NotificationRecord> getAllNotifications() {
        return notificationRecordRepository.findAll();
    }

    public List<NotificationRecord> getNotificationsByOrderNumber(String orderNumber) {
        return notificationRecordRepository.findByOrderNumberIgnoreCase(orderNumber);
    }

    public List<NotificationRecord> getNotificationsByUserId(String userId) {
        return notificationRecordRepository.findByUserIdIgnoreCase(userId);
    }
}
