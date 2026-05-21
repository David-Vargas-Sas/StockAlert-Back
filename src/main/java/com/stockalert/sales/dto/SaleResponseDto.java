package com.stockalert.sales.dto;

import com.stockalert.sales.model.SaleStatus;
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
    private Long customerId;
    private String customerName;
    private String saleNumber;
    private String invoiceNumber;
    private LocalDateTime saleDate;
    private String createdBy;
    private String sellerName;
    private SaleStatus status;
    private String statusLabel;
    private LocalDateTime cancelledAt;
    private String cancelledBy;
    private BigDecimal total;
    private List<SaleDetailResponseDto> details;
}
