package com.dropzone.orderservice.service;

import com.dropzone.orderservice.dto.CreateOrderRequest;
import com.dropzone.orderservice.dto.OrderDto;
import com.dropzone.orderservice.model.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderDto createOrder(CreateOrderRequest request);
    OrderDto createOrder(CreateOrderRequest request, String idempotencyKeyHeader);
    OrderDto getOrderById(Long id);
    OrderDto getOrderByOrderNumber(String orderNumber);
    String getFormattedUserViewByOrderNumber(String orderNumber);
    List<OrderDto> getOrdersByUserId(String userId);

    OrderDto reserveOrder(Long id, String reservationId);
    OrderDto markPaymentPending(Long id);
    OrderDto markPaid(Long id, String paymentId);
    OrderDto markConfirmed(Long id);
    OrderDto markFailed(Long id, String reason);

    OrderDto updateOrderStatus(Long id, OrderStatus targetStatus, String paymentId);
}
