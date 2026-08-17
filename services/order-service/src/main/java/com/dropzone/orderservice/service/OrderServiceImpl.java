package com.dropzone.orderservice.service;

import com.dropzone.orderservice.dto.CreateOrderRequest;
import com.dropzone.orderservice.exception.InvalidOrderStateException;
import com.dropzone.orderservice.exception.OrderNotFoundException;
import com.dropzone.orderservice.model.Order;
import com.dropzone.orderservice.model.OrderDto;
import com.dropzone.orderservice.model.OrderStatus;
import com.dropzone.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY_PREFIX = "cache:order:";
    private static final String CACHE_KEY_NUM_PREFIX = "cache:order:number:";

    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        String orderNumber = request.getCustomOrderNumber();
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            orderNumber = "DZ" + (10000 + new Random().nextInt(90000));
        } else if (orderRepository.findByOrderNumber(orderNumber).isPresent()) {
            orderNumber = orderNumber + "_" + (System.currentTimeMillis() % 10000);
        }

        BigDecimal unitPrice = request.getUnitPrice() != null ? request.getUnitPrice() : BigDecimal.valueOf(300.00);
        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(request.getUserId() != null ? request.getUserId() : "123")
                .eventId(request.getEventId() != null ? request.getEventId() : 1L)
                .eventName(request.getEventName() != null ? request.getEventName() : "Coldplay Concert")
                .ticketCategoryId(request.getTicketCategoryId() != null ? request.getTicketCategoryId() : 1L)
                .categoryName(request.getCategoryName() != null ? request.getCategoryName() : "VIP")
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .reservationId(request.getReservationId())
                .build();

        Order saved = orderRepository.save(order);
        log.info("Order created in PENDING state with orderNumber: {}", saved.getOrderNumber());

        // PENDING -> RESERVED
        saved.setStatus(OrderStatus.RESERVED);
        saved = orderRepository.save(saved);
        log.info("Order {} transitioned to RESERVED", saved.getOrderNumber());

        // RESERVED -> PAYMENT_PENDING
        saved.setStatus(OrderStatus.PAYMENT_PENDING);
        saved = orderRepository.save(saved);
        log.info("Order {} transitioned to PAYMENT_PENDING", saved.getOrderNumber());

        OrderDto dto = mapToDto(saved);
        cacheOrder(dto);
        return dto;
    }

    @Override
    public OrderDto getOrderById(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.convertValue(cached, OrderDto.class);
            } catch (Exception ignored) {
            }
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));

        OrderDto dto = mapToDto(order);
        cacheOrder(dto);
        return dto;
    }

    @Override
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        String cleanNumber = orderNumber.startsWith("#") ? orderNumber.substring(1) : orderNumber;
        String cacheKey = CACHE_KEY_NUM_PREFIX + cleanNumber;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.convertValue(cached, OrderDto.class);
            } catch (Exception ignored) {
            }
        }

        Order order = orderRepository.findByOrderNumber(cleanNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + cleanNumber));

        OrderDto dto = mapToDto(order);
        cacheOrder(dto);
        return dto;
    }

    @Override
    public String getFormattedUserViewByOrderNumber(String orderNumber) {
        OrderDto dto = getOrderByOrderNumber(orderNumber);
        return dto.getFormattedUserView();
    }

    @Override
    public List<OrderDto> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDto reserveOrder(Long id, String reservationId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));

        validateTransition(order.getStatus(), OrderStatus.RESERVED);
        order.setStatus(OrderStatus.RESERVED);
        if (reservationId != null) {
            order.setReservationId(reservationId);
        }

        Order saved = orderRepository.save(order);
        OrderDto dto = mapToDto(saved);
        cacheOrder(dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto markPaymentPending(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));

        validateTransition(order.getStatus(), OrderStatus.PAYMENT_PENDING);
        order.setStatus(OrderStatus.PAYMENT_PENDING);

        Order saved = orderRepository.save(order);
        OrderDto dto = mapToDto(saved);
        cacheOrder(dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto markPaid(Long id, String paymentId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));

        validateTransition(order.getStatus(), OrderStatus.PAID);
        order.setStatus(OrderStatus.PAID);
        if (paymentId != null) {
            order.setPaymentId(paymentId);
        }

        Order saved = orderRepository.save(order);
        log.info("Order {} transitioned PAYMENT_PENDING -> PAID", saved.getOrderNumber());

        // Auto transition PAID -> CONFIRMED
        saved.setStatus(OrderStatus.CONFIRMED);
        saved = orderRepository.save(saved);
        log.info("Order {} transitioned PAID -> CONFIRMED", saved.getOrderNumber());

        OrderDto dto = mapToDto(saved);
        cacheOrder(dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto markConfirmed(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));

        validateTransition(order.getStatus(), OrderStatus.CONFIRMED);
        order.setStatus(OrderStatus.CONFIRMED);

        Order saved = orderRepository.save(order);
        OrderDto dto = mapToDto(saved);
        cacheOrder(dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto markFailed(Long id, String reason) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));

        validateTransition(order.getStatus(), OrderStatus.FAILED);
        order.setStatus(OrderStatus.FAILED);

        Order saved = orderRepository.save(order);
        log.info("Order {} transitioned to FAILED. Reason: {}", saved.getOrderNumber(), reason);

        OrderDto dto = mapToDto(saved);
        cacheOrder(dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long id, OrderStatus targetStatus, String paymentId) {
        if (targetStatus == OrderStatus.RESERVED) {
            return reserveOrder(id, null);
        } else if (targetStatus == OrderStatus.PAYMENT_PENDING) {
            return markPaymentPending(id);
        } else if (targetStatus == OrderStatus.PAID) {
            return markPaid(id, paymentId);
        } else if (targetStatus == OrderStatus.CONFIRMED) {
            return markConfirmed(id);
        } else if (targetStatus == OrderStatus.FAILED) {
            return markFailed(id, "Payment or reservation failed");
        } else {
            throw new InvalidOrderStateException("Unsupported target status: " + targetStatus);
        }
    }

    private void validateTransition(OrderStatus current, OrderStatus target) {
        if (current == target) {
            return;
        }

        boolean valid = false;
        switch (current) {
            case PENDING:
                valid = (target == OrderStatus.RESERVED || target == OrderStatus.FAILED);
                break;
            case RESERVED:
                valid = (target == OrderStatus.PAYMENT_PENDING || target == OrderStatus.FAILED);
                break;
            case PAYMENT_PENDING:
                valid = (target == OrderStatus.PAID || target == OrderStatus.FAILED);
                break;
            case PAID:
                valid = (target == OrderStatus.CONFIRMED || target == OrderStatus.FAILED);
                break;
            case CONFIRMED:
            case FAILED:
                valid = false;
                break;
        }

        if (!valid) {
            throw new InvalidOrderStateException(
                    String.format("Invalid order state transition from %s to %s", current, target)
            );
        }
    }

    private void cacheOrder(OrderDto dto) {
        try {
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + dto.getId(), dto, 10, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(CACHE_KEY_NUM_PREFIX + dto.getOrderNumber(), dto, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to cache order in Redis: {}", e.getMessage());
        }
    }

    private OrderDto mapToDto(Order order) {
        String formattedView = OrderDto.buildFormattedUserView(
                order.getOrderNumber(),
                order.getEventName(),
                order.getCategoryName(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus()
        );

        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .eventId(order.getEventId())
                .eventName(order.getEventName())
                .ticketCategoryId(order.getTicketCategoryId())
                .categoryName(order.getCategoryName())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .reservationId(order.getReservationId())
                .paymentId(order.getPaymentId())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .formattedUserView(formattedView)
                .build();
    }
}
