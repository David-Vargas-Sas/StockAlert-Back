package com.stockalert.purchases.repository;

import com.stockalert.purchases.model.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByCompanyId(Long companyId);

    Page<Purchase> findByCompanyId(Long companyId, Pageable pageable);

    Optional<Purchase> findByIdAndCompanyId(Long id, Long companyId);
}
