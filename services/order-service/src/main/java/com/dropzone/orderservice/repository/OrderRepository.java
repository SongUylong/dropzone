package com.dropzone.orderservice.repository;

import com.dropzone.orderservice.model.Order;
import com.dropzone.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByIdempotencyKey(String idempotencyKey);
    List<Order> findByUserId(String userId);
    List<Order> findByStatus(OrderStatus status);
}
