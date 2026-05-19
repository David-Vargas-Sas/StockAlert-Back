package com.stockalert.inventory.dto;

import com.stockalert.inventory.model.InventoryMovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementResponseDto {

    private Long id;
    private Long companyId;
    private Long productId;
    private String productName;
    private InventoryMovementType type;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private String referenceType;
    private Long referenceId;
    private String notes;
    private LocalDateTime createdAt;
    private String createdBy;
}
