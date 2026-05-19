package com.stockalert.purchases.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseItemCreateDto {

    @NotNull(message = "El id del producto es obligatorio")
    private Long productId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    private Integer quantity;

    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El costo unitario debe ser mayor que cero")
    private BigDecimal unitCost;
}
