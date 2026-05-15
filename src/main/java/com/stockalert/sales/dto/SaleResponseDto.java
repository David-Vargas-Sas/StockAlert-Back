package com.stockalert.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponseDto {

    private Long id;
    private Long companyId;
    private LocalDateTime saleDate;
    private BigDecimal total;
    private List<SaleDetailResponseDto> details;
}
