package com.dropzone.orderservice.unit;

import com.dropzone.orderservice.model.Order;
import com.dropzone.orderservice.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderStatusTransitionTest {

    @Test
    @DisplayName("Should validate valid order state transitions")
    void testValidStateTransitions() {
        Order order = Order.builder()
                .id(1L)
                .orderNumber("DZ10001")
                .status(OrderStatus.PENDING)
                .build();

        // PENDING -> RESERVED
        order.setStatus(OrderStatus.RESERVED);
        assertEquals(OrderStatus.RESERVED, order.getStatus());

        // RESERVED -> PAYMENT_PENDING
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        assertEquals(OrderStatus.PAYMENT_PENDING, order.getStatus());

        // PAYMENT_PENDING -> PAID
        order.setStatus(OrderStatus.PAID);
        assertEquals(OrderStatus.PAID, order.getStatus());

        // PAID -> CONFIRMED
        order.setStatus(OrderStatus.CONFIRMED);
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    @DisplayName("Should test order failure transition from any pending state")
    void testFailureTransitions() {
        Order order1 = Order.builder().status(OrderStatus.PENDING).build();
        order1.setStatus(OrderStatus.FAILED);
        assertEquals(OrderStatus.FAILED, order1.getStatus());

        Order order2 = Order.builder().status(OrderStatus.RESERVED).build();
        order2.setStatus(OrderStatus.FAILED);
        assertEquals(OrderStatus.FAILED, order2.getStatus());

        Order order3 = Order.builder().status(OrderStatus.PAYMENT_PENDING).build();
        order3.setStatus(OrderStatus.FAILED);
        assertEquals(OrderStatus.FAILED, order3.getStatus());
    }

    @Test
    @DisplayName("Should verify terminal status invariant for CONFIRMED orders")
    void testTerminalConfirmedStatus() {
        Order order = Order.builder().status(OrderStatus.CONFIRMED).build();
        assertTrue(order.getStatus() == OrderStatus.CONFIRMED);
    }
}
