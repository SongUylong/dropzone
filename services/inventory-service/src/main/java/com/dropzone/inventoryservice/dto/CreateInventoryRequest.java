package com.dropzone.inventoryservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInventoryRequest {
    @NotNull(message = "eventId is required")
    private Long eventId;

    @NotNull(message = "ticketCategoryId is required")
    private Long ticketCategoryId;

    @NotBlank(message = "categoryName is required")
    private String categoryName;

    @NotNull(message = "totalQuantity is required")
    @Min(value = 1, message = "totalQuantity must be at least 1")
    private Integer totalQuantity;
}
