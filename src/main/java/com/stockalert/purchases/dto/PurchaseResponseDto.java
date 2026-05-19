package com.stockalert.purchases.dto;

import com.stockalert.purchases.model.PurchaseStatus;
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
public class PurchaseResponseDto {

    private Long id;
    private Long companyId;
    private Long supplierId;
    private String supplierName;
    private LocalDateTime purchaseDate;
    private PurchaseStatus status;
    private BigDecimal total;
    private LocalDateTime cancelledAt;
    private String cancelledBy;
    private List<PurchaseDetailResponseDto> details;
}
