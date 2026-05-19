package com.stockalert.purchases.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDetailResponseDto {

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitCost;
    private BigDecimal subtotal;
}
