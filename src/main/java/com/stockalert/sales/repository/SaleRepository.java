package com.stockalert.sales.repository;

import com.stockalert.sales.model.Sale;
import com.stockalert.sales.model.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByCompanyId(Long companyId);

    Page<Sale> findByCompanyId(Long companyId, Pageable pageable);

    Page<Sale> findByCompanyIdAndStatus(Long companyId, SaleStatus status, Pageable pageable);

    Page<Sale> findByCompanyIdAndSaleDateBetween(Long companyId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Sale> findByCompanyIdAndStatusAndSaleDateBetween(Long companyId, SaleStatus status, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Sale> findByCompanyIdAndSaleDateGreaterThanEqual(Long companyId, LocalDateTime start, Pageable pageable);

    Page<Sale> findByCompanyIdAndSaleDateLessThanEqual(Long companyId, LocalDateTime end, Pageable pageable);

    Page<Sale> findByCompanyIdAndStatusAndSaleDateGreaterThanEqual(Long companyId, SaleStatus status, LocalDateTime start, Pageable pageable);

    Page<Sale> findByCompanyIdAndStatusAndSaleDateLessThanEqual(Long companyId, SaleStatus status, LocalDateTime end, Pageable pageable);

    List<Sale> findByCompanyIdAndStatusAndSaleDateBetween(Long companyId, SaleStatus status, LocalDateTime start, LocalDateTime end);

    List<Sale> findTop5ByCompanyIdOrderBySaleDateDesc(Long companyId);

    List<Sale> findByCompanyIdAndStatusOrderBySaleDateDesc(Long companyId, SaleStatus status, Pageable pageable);

    Optional<Sale> findByIdAndCompanyId(Long id, Long companyId);

    long countByCompanyId(Long companyId);

    @org.springframework.data.jpa.repository.Query("select coalesce(sum(s.total), 0) from Sale s where s.company.id = :companyId and s.status = com.stockalert.sales.model.SaleStatus.ACTIVE and s.saleDate between :start and :end")
    java.math.BigDecimal sumActiveSalesByCompanyIdAndDateBetween(@Param("companyId") Long companyId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByCompanyIdAndSaleDateBetween(Long companyId, LocalDateTime start, LocalDateTime end);

    long countByCompanyIdAndStatusAndSaleDateBetween(Long companyId, SaleStatus status, LocalDateTime start, LocalDateTime end);
}
