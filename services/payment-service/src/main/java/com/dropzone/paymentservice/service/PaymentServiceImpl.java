package com.dropzone.paymentservice.service;

import com.dropzone.paymentservice.dto.PaymentCallbackRequest;
import com.dropzone.paymentservice.dto.PaymentDto;
import com.dropzone.paymentservice.dto.ProcessPaymentRequest;
import com.dropzone.paymentservice.event.PaymentEvent;
import com.dropzone.paymentservice.event.PaymentEventProducer;
import com.dropzone.paymentservice.exception.PaymentNotFoundException;
import com.dropzone.paymentservice.model.Payment;
import com.dropzone.paymentservice.model.PaymentMode;
import com.dropzone.paymentservice.model.PaymentStatus;
import com.dropzone.paymentservice.provider.MockPayProvider;
import com.dropzone.paymentservice.provider.MockPayResponse;
import com.dropzone.paymentservice.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final MockPayProvider mockPayProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final PaymentEventProducer paymentEventProducer;

    private static final String CACHE_KEY_PREFIX = "cache:payment:";
    private static final String CACHE_KEY_ORDER_PREFIX = "cache:payment:order:";
    private static final String CALLBACK_IDEMPOTENCY_PREFIX = "idempotency:callback:";

    @Override
    @Transactional
    public PaymentDto processPayment(ProcessPaymentRequest request) {
        String orderNumber = request.getOrderNumber();
        if (orderNumber == null || orderNumber.isBlank()) {
            orderNumber = "DZ" + System.currentTimeMillis();
        }
        String cleanOrderNumber = orderNumber.startsWith("#") ? orderNumber.substring(1) : orderNumber;
        String userId = (request.getUserId() != null && !request.getUserId().isBlank()) ? request.getUserId() : "123";
        BigDecimal amount = request.getAmount() != null ? request.getAmount() : BigDecimal.valueOf(600.00);
        PaymentMode mode = request.getMode() != null ? request.getMode() : PaymentMode.SUCCESS;

        String paymentId = "PAY_" + cleanOrderNumber + "_" + System.currentTimeMillis() % 10000;

        log.info("Processing Payment {} for Order {} User {} with Amount ${} [Mode: {}]", paymentId, cleanOrderNumber, userId, amount, mode);

        // Produce PaymentStarted Kafka Event
        paymentEventProducer.sendPaymentEvent(PaymentEvent.builder()
                .eventType("PaymentStarted")
                .paymentId(paymentId)
                .orderNumber(cleanOrderNumber)
                .userId(userId)
                .amount(amount)
                .currency("USD")
                .status("PENDING")
                .timestamp(Instant.now())
                .build());

        MockPayResponse response = mockPayProvider.processPayment(cleanOrderNumber, amount, mode, request.getCustomFailureReason());

        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .orderNumber(cleanOrderNumber)
                .amount(amount)
                .currency("USD")
                .status(response.getStatus())
                .mode(mode)
                .failureReason(response.getFailureReason())
                .transactionId(response.getTransactionId())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment saved with ID {}, Status: {}", saved.getPaymentId(), saved.getStatus());

        String eventType = (saved.getStatus() == PaymentStatus.SUCCESS) ? "PaymentCompleted" : "PaymentFailed";
        paymentEventProducer.sendPaymentEvent(PaymentEvent.builder()
                .eventType(eventType)
                .paymentId(saved.getPaymentId())
                .orderNumber(cleanOrderNumber)
                .userId(userId)
                .amount(amount)
                .currency("USD")
                .status(saved.getStatus().name())
                .failureReason(saved.getFailureReason())
                .transactionId(saved.getTransactionId())
                .timestamp(Instant.now())
                .build());

        PaymentDto dto = mapToDto(saved);
        cachePayment(dto);
        return dto;
    }

    @Override
    @Transactional
    public PaymentDto handleCallback(PaymentCallbackRequest callback) {
        log.info("Handling Payment Callback for PaymentId: {}, TxnId: {}, Status: {}",
                callback.getPaymentId(), callback.getTransactionId(), callback.getStatus());

        String idempotencyKey = CALLBACK_IDEMPOTENCY_PREFIX + callback.getPaymentId() + ":" + callback.getTransactionId();
        Boolean firstTime = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "PROCESSED", 24, TimeUnit.HOURS);

        if (Boolean.FALSE.equals(firstTime)) {
            log.warn("Duplicate Callback detected for PaymentId: {}! Returning existing status.", callback.getPaymentId());
            Optional<Payment> existing = paymentRepository.findByPaymentId(callback.getPaymentId());
            if (existing.isPresent()) {
                PaymentDto dto = mapToDto(existing.get());
                dto.setFailureReason("Duplicate callback ignored");
                return dto;
            }
        }

        Payment payment = paymentRepository.findByPaymentId(callback.getPaymentId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + callback.getPaymentId()));

        if (callback.getStatus() != null) {
            payment.setStatus(callback.getStatus());
        }
        if (callback.getTransactionId() != null) {
            payment.setTransactionId(callback.getTransactionId());
        }
        if (callback.getFailureReason() != null) {
            payment.setFailureReason(callback.getFailureReason());
        }

        Payment updated = paymentRepository.save(payment);

        String eventType = (updated.getStatus() == PaymentStatus.SUCCESS) ? "PaymentCompleted" : "PaymentFailed";
        paymentEventProducer.sendPaymentEvent(PaymentEvent.builder()
                .eventType(eventType)
                .paymentId(updated.getPaymentId())
                .orderNumber(updated.getOrderNumber())
                .userId("123")
                .amount(updated.getAmount())
                .currency(updated.getCurrency())
                .status(updated.getStatus().name())
                .failureReason(updated.getFailureReason())
                .transactionId(updated.getTransactionId())
                .timestamp(Instant.now())
                .build());

        PaymentDto dto = mapToDto(updated);
        cachePayment(dto);
        return dto;
    }

    @Override
    public PaymentDto getPaymentById(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.convertValue(cached, PaymentDto.class);
            } catch (Exception ignored) {
            }
        }

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + id));

        PaymentDto dto = mapToDto(payment);
        cachePayment(dto);
        return dto;
    }

    @Override
    public PaymentDto getPaymentByOrderNumber(String orderNumber) {
        String cleanNum = orderNumber.startsWith("#") ? orderNumber.substring(1) : orderNumber;
        String cacheKey = CACHE_KEY_ORDER_PREFIX + cleanNum;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.convertValue(cached, PaymentDto.class);
            } catch (Exception ignored) {
            }
        }

        Payment payment = paymentRepository.findByOrderNumber(cleanNum)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + cleanNum));

        PaymentDto dto = mapToDto(payment);
        cachePayment(dto);
        return dto;
    }

    @Override
    public String getFormattedUserViewByOrderNumber(String orderNumber) {
        PaymentDto dto = getPaymentByOrderNumber(orderNumber);
        return dto.getFormattedUserView();
    }

    private void cachePayment(PaymentDto dto) {
        try {
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + dto.getId(), dto, 10, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(CACHE_KEY_ORDER_PREFIX + dto.getOrderNumber(), dto, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to cache payment in Redis: {}", e.getMessage());
        }
    }

    private PaymentDto mapToDto(Payment payment) {
        String formattedView = PaymentDto.buildFormattedUserView(
                payment.getOrderNumber(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getFailureReason()
        );

        return PaymentDto.builder()
                .id(payment.getId())
                .paymentId(payment.getPaymentId())
                .orderNumber(payment.getOrderNumber())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .mode(payment.getMode())
                .failureReason(payment.getFailureReason())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .formattedUserView(formattedView)
                .build();
    }
}
