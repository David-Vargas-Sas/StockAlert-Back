package com.stockalert.alerts.dto;

import com.stockalert.alerts.model.AlertStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAlertResponseDto {

    private Long id;
    private Long companyId;
    private Long productId;
    private String productName;
    private String message;
    private AlertStatus status;
    private String statusLabel;
    private LocalDateTime createdAt;
}
