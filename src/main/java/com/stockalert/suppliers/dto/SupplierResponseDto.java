package com.stockalert.suppliers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponseDto {

    private Long id;
    private Long companyId;
    private String name;
    private String taxId;
    private String contactName;
    private String email;
    private String phone;
    private String address;
    private Boolean active;
    private LocalDateTime createdAt;
}
