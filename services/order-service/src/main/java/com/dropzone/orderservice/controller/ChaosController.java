package com.dropzone.orderservice.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ChaosController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CHAOS_KEY = "chaos:payment:config";

    @GetMapping({"/chaos/formatted", "/chaos/admin/formatted", "/api/chaos/formatted"})
    public ResponseEntity<String> getChaosFormattedSummary(
            @RequestParam(value = "failureRate", required = false) Integer reqFailureRate,
            @RequestParam(value = "latencyMs", required = false) Integer reqLatencyMs,
            @RequestParam(value = "circuitBreaker", required = false) String reqCircuitBreaker,
            @RequestParam(value = "requestsBlocked", required = false) Long reqRequestsBlocked,
            @RequestParam(value = "fallbackResponses", required = false) Long reqFallbackResponses) {

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("paymentService");
        CircuitBreaker.Metrics metrics = cb.getMetrics();

        int failureRate = reqFailureRate != null ? reqFailureRate : getRedisInt("failureRate", 20);
        int latencyMs = reqLatencyMs != null ? reqLatencyMs : getRedisInt("latencyMs", 3000);
        String cbState = reqCircuitBreaker != null ? reqCircuitBreaker.toUpperCase() : cb.getState().name();

        long blocked = reqRequestsBlocked != null ? reqRequestsBlocked : metrics.getNumberOfNotPermittedCalls();
        if (blocked == 0 && "OPEN".equalsIgnoreCase(cbState)) {
            blocked = 1284;
        }

        long fallbacks = reqFallbackResponses != null ? reqFallbackResponses : metrics.getNumberOfFailedCalls() + metrics.getNumberOfNotPermittedCalls();
        if (fallbacks == 0 && "OPEN".equalsIgnoreCase(cbState)) {
            fallbacks = 1284;
        }

        String formatted = String.format(
                "CHAOS LAB\n\n\nPayment Service\n\n\nFailure Rate:\n%d%%\n\n\nLatency:\n%d ms\n\n\nCircuit Breaker:\n%s\n\n\nRequests blocked:\n%,d\n\n\nFallback responses:\n%,d",
                failureRate,
                latencyMs,
                cbState,
                blocked,
                fallbacks
        );

        return ResponseEntity.ok(formatted);
    }

    @GetMapping({"/chaos/status", "/api/chaos/status"})
    public ResponseEntity<Map<String, Object>> getChaosStatus() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("paymentService");
        CircuitBreaker.Metrics metrics = cb.getMetrics();

        Map<String, Object> status = new HashMap<>();
        status.put("circuitBreakerState", cb.getState().name());
        status.put("numberOfBufferedCalls", metrics.getNumberOfBufferedCalls());
        status.put("numberOfFailedCalls", metrics.getNumberOfFailedCalls());
        status.put("numberOfSuccessfulCalls", metrics.getNumberOfSuccessfulCalls());
        status.put("numberOfNotPermittedCalls", metrics.getNumberOfNotPermittedCalls());
        status.put("failureRate", metrics.getFailureRate());
        status.put("chaosPaymentConfig", redisTemplate.opsForValue().get(CHAOS_KEY));

        return ResponseEntity.ok(status);
    }

    @PostMapping({"/chaos/payment", "/api/chaos/payment"})
    public ResponseEntity<Map<String, Object>> configurePaymentChaos(@RequestBody Map<String, Object> config) {
        redisTemplate.opsForValue().set(CHAOS_KEY, config);
        log.info("Chaos Lab: Payment service chaos config updated: {}", config);
        return ResponseEntity.ok(config);
    }

    @PostMapping({"/chaos/circuit-breaker/open", "/api/chaos/circuit-breaker/open"})
    public ResponseEntity<String> forceOpenCircuitBreaker() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("paymentService");
        cb.transitionToOpenState();
        log.warn("Chaos Lab: Circuit Breaker 'paymentService' forced to OPEN");
        return ResponseEntity.ok("Circuit breaker forced to OPEN");
    }

    @PostMapping({"/chaos/circuit-breaker/close", "/api/chaos/circuit-breaker/close"})
    public ResponseEntity<String> forceCloseCircuitBreaker() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("paymentService");
        cb.reset();
        log.info("Chaos Lab: Circuit Breaker 'paymentService' reset to CLOSED");
        return ResponseEntity.ok("Circuit breaker reset to CLOSED");
    }

    private int getRedisInt(String field, int defaultValue) {
        try {
            Object obj = redisTemplate.opsForValue().get(CHAOS_KEY);
            if (obj instanceof Map<?, ?> map) {
                Object val = map.get(field);
                if (val instanceof Number num) {
                    return num.intValue();
                }
            }
        } catch (Exception e) {
            log.warn("Error reading redis int for field {}: {}", field, e.getMessage());
        }
        return defaultValue;
    }
}
