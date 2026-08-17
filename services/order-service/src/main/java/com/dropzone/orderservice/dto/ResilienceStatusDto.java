package com.dropzone.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResilienceStatusDto {
    private String circuitBreakerName;
    private String state; // CLOSED, OPEN, HALF_OPEN
    private float failureRate;
    private float slowCallRate;
    private int numberOfBufferedCalls;
    private int numberOfFailedCalls;
    private int numberOfSuccessfulCalls;
    private long numberOfNotPermittedCalls;
    private boolean isCircuitBreakerOpen;
}
