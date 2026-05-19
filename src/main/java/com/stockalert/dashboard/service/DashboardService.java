package com.stockalert.dashboard.service;

import com.stockalert.alerts.model.AlertStatus;
import com.stockalert.alerts.repository.StockAlertRepository;
import com.stockalert.dashboard.dto.DashboardSummaryDto;
import com.stockalert.products.repository.ProductRepository;
import com.stockalert.sales.repository.SaleRepository;
import com.stockalert.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final StockAlertRepository stockAlertRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public DashboardSummaryDto summary() {
        Long companyId = currentUserService.getCompanyId();
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);
        return DashboardSummaryDto.builder()
                .todaySalesTotal(saleRepository.sumActiveSalesByCompanyIdAndDateBetween(companyId, start, end))
                .todaySalesCount(saleRepository.countByCompanyIdAndSaleDateBetween(companyId, start, end))
                .activeProducts(productRepository.countByCompanyIdAndActive(companyId, true))
                .lowStockProducts((long) productRepository.findLowStockProductsByCompanyId(companyId).size())
                .activeAlerts((long) stockAlertRepository.findByCompanyIdAndStatus(companyId, AlertStatus.ACTIVE).size())
                .stockValue(productRepository.calculateStockValueByCompanyId(companyId))
                .build();
    }
}
