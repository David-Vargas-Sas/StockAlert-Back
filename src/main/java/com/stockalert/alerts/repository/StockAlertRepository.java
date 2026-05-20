package com.stockalert.alerts.repository;

import com.stockalert.alerts.model.AlertStatus;
import com.stockalert.alerts.model.StockAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;

public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {

    boolean existsByProductIdAndStatus(Long productId, AlertStatus status);

    List<StockAlert> findByStatus(AlertStatus status);

    boolean existsByProductIdAndCompanyIdAndStatus(Long productId, Long companyId, AlertStatus status);

    List<StockAlert> findByCompanyId(Long companyId);

    Page<StockAlert> findByCompanyId(Long companyId, Pageable pageable);

    List<StockAlert> findByCompanyIdAndStatus(Long companyId, AlertStatus status);

    long countByCompanyIdAndStatus(Long companyId, AlertStatus status);

    long countByCompanyIdAndCreatedAtBetween(Long companyId, LocalDateTime start, LocalDateTime end);

    java.util.Optional<StockAlert> findByIdAndCompanyId(Long id, Long companyId);
}
