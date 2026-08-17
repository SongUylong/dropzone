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
public class WaitingRoomStatusDto implements Serializable {
    private Long eventId;
    private String userId;
    private Long queuePosition; // 1-based rank
    private Long totalInQueue;
    private boolean isAdmitted;
    private String admissionToken;
}
