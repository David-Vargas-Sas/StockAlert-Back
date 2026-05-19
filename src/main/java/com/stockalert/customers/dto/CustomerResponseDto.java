package com.stockalert.customers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseDto {

    private Long id;
    private Long companyId;
    private String fullName;
    private String documentNumber;
    private String email;
    private String phone;
    private String address;
    private Boolean active;
    private LocalDateTime createdAt;
}
