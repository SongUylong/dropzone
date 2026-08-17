package com.dropzone.orderservice.service;

import com.dropzone.orderservice.event.OrderEventProducer;
import com.dropzone.orderservice.model.OutboxEvent;
import com.dropzone.orderservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final OrderEventProducer orderEventProducer;
    private final ObjectMapper objectMapper;

    /**
     * Saves outbox event within current active database transaction.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEvent saveOutboxEvent(String aggregateType, String aggregateId, String type, Object payloadObject) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payloadObject);
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .type(type)
                    .payload(payloadJson)
                    .published(false)
                    .createdAt(Instant.now())
                    .build();

            OutboxEvent saved = outboxEventRepository.save(outboxEvent);
            log.info("[Transactional Outbox] Saved outbox event {} for Aggregate: {}, ID: {}, Type: {} in DB transaction",
                    saved.getId(), aggregateType, aggregateId, type);
            return saved;
        } catch (Exception e) {
            log.error("Failed to serialize and save outbox event: {}", e.getMessage(), e);
            throw new RuntimeException("Outbox event creation failed", e);
        }
    }

    /**
     * Mark an outbox event as published/processed.
     */
    @Transactional
    public void markAsPublished(UUID outboxEventId) {
        outboxEventRepository.findById(outboxEventId).ifPresent(event -> {
            event.setPublished(true);
            event.setProcessedAt(Instant.now());
            outboxEventRepository.save(event);
            log.info("[Transactional Outbox] Event {} marked as PUBLISHED/PROCESSED", outboxEventId);
        });
    }

    /**
     * Scheduled processor / relay that ensures all pending outbox events are published to Kafka and marked.
     */
    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void processPendingOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByPublishedFalseOrderByCreatedAtAsc();
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("[Transactional Outbox Relay] Found {} pending outbox event(s) to publish to Kafka", pendingEvents.size());
        for (OutboxEvent event : pendingEvents) {
            try {
                orderEventProducer.sendRawOrderEvent(event.getPayload());
                event.setPublished(true);
                event.setProcessedAt(Instant.now());
                outboxEventRepository.save(event);
                log.info("[Transactional Outbox Relay] Outbox Event {} (Type: {}, Aggregate: {}) -> Kafka -> MARKED PUBLISHED",
                        event.getId(), event.getType(), event.getAggregateId());
            } catch (Exception e) {
                log.error("[Transactional Outbox Relay] Failed to publish Outbox Event {}: {}", event.getId(), e.getMessage());
            }
        }
    }

    public List<OutboxEvent> getAllOutboxEvents() {
        return outboxEventRepository.findAll();
    }
}
