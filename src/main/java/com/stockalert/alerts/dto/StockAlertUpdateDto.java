package com.stockalert.alerts.dto;

import com.stockalert.alerts.model.AlertStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAlertUpdateDto {

    @NotNull(message = "El estado de la alerta es obligatorio")
    private AlertStatus status;
}
