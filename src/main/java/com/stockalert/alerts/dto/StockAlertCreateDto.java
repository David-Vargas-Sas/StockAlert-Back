package com.stockalert.alerts.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAlertCreateDto {

    @NotNull(message = "El id del producto es obligatorio")
    private Long productId;

    @Size(max = 255, message = "El mensaje no puede superar 255 caracteres")
    private String message;
}
