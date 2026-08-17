package com.dropzone.orderservice.event;

import com.dropzone.orderservice.model.Order;
import com.dropzone.orderservice.model.OrderStatus;
import com.dropzone.orderservice.repository.OrderRepository;
import com.dropzone.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void handlePaymentEvent(String message) {
        log.info("Order Service received Kafka payment-event: {}", message);
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.has("eventType") ? node.get("eventType").asText() : "";
            String orderNumber = node.has("orderNumber") ? node.get("orderNumber").asText() : "";
            String paymentId = node.has("paymentId") ? node.get("paymentId").asText() : "";
            String failureReason = node.has("failureReason") ? node.get("failureReason").asText() : "Payment failed";

            if (orderNumber.isBlank()) {
                return;
            }

            Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
            if (orderOpt.isEmpty()) {
                log.warn("Received payment event for unknown orderNumber: {}", orderNumber);
                return;
            }
            Order order = orderOpt.get();

            if ("PaymentCompleted".equalsIgnoreCase(eventType)) {
                log.info("Kafka Event PaymentCompleted -> Updating Order {} to PAID/CONFIRMED", orderNumber);
                if (order.getStatus() == OrderStatus.PAYMENT_PENDING || order.getStatus() == OrderStatus.RESERVED || order.getStatus() == OrderStatus.PENDING) {
                    orderService.markPaid(order.getId(), paymentId);
                }
            } else if ("PaymentFailed".equalsIgnoreCase(eventType)) {
                log.info("Kafka Event PaymentFailed -> Updating Order {} to FAILED", orderNumber);
                if (order.getStatus() == OrderStatus.PAYMENT_PENDING || order.getStatus() == OrderStatus.RESERVED || order.getStatus() == OrderStatus.PENDING) {
                    orderService.markFailed(order.getId(), failureReason);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process payment event in Order Service: {}", e.getMessage(), e);
        }
    }
}
