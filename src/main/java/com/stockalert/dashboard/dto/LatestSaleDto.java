package com.stockalert.dashboard.dto;

import com.stockalert.sales.model.SaleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatestSaleDto {

    private Long id;
    private String saleNumber;
    private Long customerId;
    private String customerName;
    private BigDecimal total;
    private SaleStatus status;
    private String statusLabel;
    private LocalDateTime saleDate;
}
