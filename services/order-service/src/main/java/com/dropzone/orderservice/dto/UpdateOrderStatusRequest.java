package com.dropzone.orderservice.dto;

import com.dropzone.orderservice.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {

    private OrderStatus status;
    private String paymentId;
    private String failureReason;
}
