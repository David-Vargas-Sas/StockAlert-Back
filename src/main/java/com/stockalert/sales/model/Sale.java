package com.stockalert.sales.model;

import com.stockalert.companies.model.Company;
import com.stockalert.customers.model.Customer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "sale_number", length = 30)
    private String saleNumber;

    @Column(name = "invoice_number", length = 30)
    private String invoiceNumber;

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'ACTIVE'")
    private SaleStatus status;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by", length = 100)
    private String cancelledBy;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Builder.Default
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleDetail> details = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (status == null) {
            status = SaleStatus.ACTIVE;
        }
        if (saleDate == null) {
            saleDate = LocalDateTime.now();
        }
    }

    public void addDetail(SaleDetail detail) {
        details.add(detail);
        detail.setSale(this);
    }
}
