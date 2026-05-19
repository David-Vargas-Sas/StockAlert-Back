package com.stockalert.inventory.repository;

import com.stockalert.inventory.model.InventoryMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    Page<InventoryMovement> findByCompanyId(Long companyId, Pageable pageable);

    Page<InventoryMovement> findByCompanyIdAndProductId(Long companyId, Long productId, Pageable pageable);

    List<InventoryMovement> findByCompanyIdAndCreatedAtBetween(Long companyId, LocalDateTime start, LocalDateTime end);
}
