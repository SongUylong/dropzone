package com.dropzone.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChaosConfigDto {

    private int failureRate;          // Percentage e.g. 20
    private int latencyMs;            // Delay e.g. 3000
    private boolean http500;          // Return HTTP 500
    private boolean timeout;          // Delay 10000ms
    private boolean duplicateCallback; // Trigger duplicate callback
    private boolean disabled;         // Disable payment service entirely
}
