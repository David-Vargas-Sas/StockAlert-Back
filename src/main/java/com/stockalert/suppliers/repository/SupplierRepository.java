package com.stockalert.suppliers.repository;

import com.stockalert.suppliers.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByCompanyId(Long companyId);

    Page<Supplier> findByCompanyId(Long companyId, Pageable pageable);

    Optional<Supplier> findByIdAndCompanyId(Long id, Long companyId);
}
