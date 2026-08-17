package com.dropzone.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyResponseDto implements Serializable {
    private String idempotencyKey;
    private boolean processed;
    private Object cachedResult;
    private String message;
}
