package com.stockalert.sales.repository;

import com.stockalert.sales.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByCompanyId(Long companyId);

    Page<Sale> findByCompanyId(Long companyId, Pageable pageable);

    Optional<Sale> findByIdAndCompanyId(Long id, Long companyId);
}
