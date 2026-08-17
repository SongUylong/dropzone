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
public class RateLimitStatusDto implements Serializable {
    private String key;
    private boolean allowed;
    private long currentCount;
    private long limit;
    private long windowSeconds;
    private long ttlSeconds;
}
