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
public class FlashSaleSummaryDto implements Serializable {
    private int users;
    private int tickets;
    private int successfulReservations;
    private int successfulOrders;
    private int rejected;
    private int oversold;
    private int negativeInventory;
    private int duplicateOrders;
    private int duplicatePayments;
    private String formattedSummary;
}
