package com.stockalert.products.dto;

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
public class ProductResponseDto {

    private Long id;
    private Long companyId;
    private Long supplierId;
    private String supplierName;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer minimumStock;
    private Boolean active;
    private LocalDateTime createdAt;
}
