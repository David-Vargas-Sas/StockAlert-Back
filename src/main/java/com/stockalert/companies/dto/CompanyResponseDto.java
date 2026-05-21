package com.stockalert.companies.dto;

import com.stockalert.companies.model.CompanyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponseDto {

    private Long id;
    private String name;
    private String tradeName;
    private String taxId;
    private String verificationDigit;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String department;
    private String country;
    private String legalRepresentative;
    private String legalRepresentativeDocument;
    private String website;
    private String logoPath;
    private CompanyStatus status;
    private String statusLabel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
