package com.dropzone.orderservice.dto;

import com.dropzone.orderservice.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {

    private Long id;
    private String orderNumber;
    private String idempotencyKey;
    private String userId;
    private Long eventId;
    private String eventName;
    private Long ticketCategoryId;
    private String categoryName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String reservationId;
    private String paymentId;
    private Instant createdAt;
    private Instant updatedAt;
    private String formattedUserView;

    public static String buildFormattedUserView(String orderNumber, String eventName, String categoryName, Integer quantity, BigDecimal totalAmount, OrderStatus status) {
        String num = orderNumber.startsWith("#") ? orderNumber : "#" + orderNumber;
        String totalFormatted = totalAmount != null ? "$" + totalAmount.stripTrailingZeros().toPlainString() : "$0";
        return String.format(
            "Order %s\n\n\n%s\n%s × %d\n\n\nTotal:\n%s\n\n\nStatus:\n%s",
            num,
            eventName,
            categoryName,
            quantity,
            totalFormatted,
            status != null ? status.name() : "PENDING"
        );
    }
}
