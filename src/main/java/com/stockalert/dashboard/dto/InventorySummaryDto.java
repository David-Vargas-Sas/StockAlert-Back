package com.stockalert.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventorySummaryDto {

    private Long activeProducts;
    private Long lowStockProducts;
    private Long outOfStockProducts;
    private BigDecimal stockValue;
}
