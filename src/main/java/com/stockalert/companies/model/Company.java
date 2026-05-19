package com.stockalert.companies.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "trade_name", length = 150)
    private String tradeName;

    @Column(name = "tax_id", length = 30)
    private String taxId;

    @Column(name = "verification_digit", length = 5)
    private String verificationDigit;

    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String country;

    @Column(name = "legal_representative", length = 150)
    private String legalRepresentative;

    @Column(name = "legal_representative_document", length = 30)
    private String legalRepresentativeDocument;

    @Column(length = 255)
    private String website;

    @Column(name = "logo_path", length = 255)
    private String logoPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'ACTIVE'")
    private CompanyStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    void prePersist() {
        if (status == null) {
            status = CompanyStatus.ACTIVE;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @jakarta.persistence.PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
