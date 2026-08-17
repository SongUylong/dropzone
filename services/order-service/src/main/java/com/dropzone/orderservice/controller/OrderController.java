package com.dropzone.orderservice.controller;

import com.dropzone.orderservice.dto.*;
import com.dropzone.orderservice.model.OrderStatus;
import com.dropzone.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String xIdempotencyKeyHeader) {
        String key = idempotencyKeyHeader != null && !idempotencyKeyHeader.isBlank() 
                ? idempotencyKeyHeader 
                : xIdempotencyKeyHeader;
        OrderDto order = orderService.createOrder(request, key);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDto> getOrderByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByOrderNumber(orderNumber));
    }

    @GetMapping("/number/{orderNumber}/view")
    public ResponseEntity<String> getFormattedUserView(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getFormattedUserViewByOrderNumber(orderNumber));
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<String> getFormattedUserViewById(@PathVariable Long id) {
        OrderDto dto = orderService.getOrderById(id);
        return ResponseEntity.ok(dto.getFormattedUserView());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getOrdersByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<OrderDto> reserveOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reservationId) {
        return ResponseEntity.ok(orderService.reserveOrder(id, reservationId));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderDto> payOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String paymentId) {
        return ResponseEntity.ok(orderService.markPaid(id, paymentId));
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<OrderDto> failOrder(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Payment failed") String reason) {
        return ResponseEntity.ok(orderService.markFailed(id, reason));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus(), request.getPaymentId()));
    }

    @PostMapping("/{id}/process-payment")
    public ResponseEntity<PaymentResponseDto> processOrderPayment(
            @PathVariable Long id,
            @RequestBody(required = false) ProcessPaymentRequestDto paymentRequest) {
        if (paymentRequest == null) {
            paymentRequest = new ProcessPaymentRequestDto();
        }
        PaymentResponseDto response = orderService.processOrderPayment(id, paymentRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/resilience-status")
    public ResponseEntity<ResilienceStatusDto> getResilienceStatus() {
        return ResponseEntity.ok(orderService.getResilienceStatus());
    }

    @PostMapping("/resilience-reset")
    public ResponseEntity<String> resetCircuitBreaker() {
        orderService.resetCircuitBreaker();
        return ResponseEntity.ok("Circuit breaker reset to CLOSED");
    }
}
