package com.stockalert.dashboard.service;

import com.stockalert.alerts.model.AlertStatus;
import com.stockalert.alerts.repository.StockAlertRepository;
import com.stockalert.dashboard.dto.DashboardSummaryDto;
import com.stockalert.dashboard.dto.InventorySummaryDto;
import com.stockalert.dashboard.dto.LatestSaleDto;
import com.stockalert.dashboard.dto.SalesPeriod;
import com.stockalert.dashboard.dto.SalesPeriodPointDto;
import com.stockalert.dashboard.dto.TopProductDto;
import com.stockalert.products.repository.ProductRepository;
import com.stockalert.sales.model.Sale;
import com.stockalert.sales.model.SaleStatus;
import com.stockalert.sales.repository.SaleDetailRepository;
import com.stockalert.sales.repository.SaleRepository;
import com.stockalert.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final ProductRepository productRepository;
    private final StockAlertRepository stockAlertRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public DashboardSummaryDto summary() {
        Long companyId = currentUserService.getCompanyId();
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);
        BigDecimal monthTotal = saleRepository.sumActiveSalesByCompanyIdAndDateBetween(
                companyId,
                currentMonth.atDay(1).atStartOfDay(),
                currentMonth.atEndOfMonth().atTime(23, 59, 59)
        );
        BigDecimal previousMonthTotal = saleRepository.sumActiveSalesByCompanyIdAndDateBetween(
                companyId,
                previousMonth.atDay(1).atStartOfDay(),
                previousMonth.atEndOfMonth().atTime(23, 59, 59)
        );
        return DashboardSummaryDto.builder()
                .todaySalesTotal(saleRepository.sumActiveSalesByCompanyIdAndDateBetween(companyId, start, end))
                .todaySalesCount(saleRepository.countByCompanyIdAndStatusAndSaleDateBetween(companyId, SaleStatus.ACTIVE, start, end))
                .monthSalesTotal(monthTotal)
                .monthSalesVariationPercentage(calculateVariation(monthTotal, previousMonthTotal))
                .activeProducts(productRepository.countByCompanyIdAndActive(companyId, true))
                .lowStockProducts((long) productRepository.findLowStockProductsByCompanyId(companyId).size())
                .outOfStockProducts(productRepository.countByCompanyIdAndStock(companyId, 0))
                .activeAlerts(stockAlertRepository.countByCompanyIdAndStatus(companyId, AlertStatus.ACTIVE))
                .todayNewAlerts(stockAlertRepository.countByCompanyIdAndCreatedAtBetween(companyId, start, end))
                .stockValue(productRepository.calculateStockValueByCompanyId(companyId))
                .build();
    }

    @Transactional(readOnly = true)
    public InventorySummaryDto inventorySummary() {
        Long companyId = currentUserService.getCompanyId();
        return InventorySummaryDto.builder()
                .activeProducts(productRepository.countByCompanyIdAndActive(companyId, true))
                .lowStockProducts((long) productRepository.findLowStockProductsByCompanyId(companyId).size())
                .outOfStockProducts(productRepository.countByCompanyIdAndStock(companyId, 0))
                .stockValue(productRepository.calculateStockValueByCompanyId(companyId))
                .build();
    }

    @Transactional(readOnly = true)
    public List<LatestSaleDto> latestSales(int limit) {
        return saleRepository.findByCompanyIdAndStatusOrderBySaleDateDesc(
                        currentUserService.getCompanyId(),
                        SaleStatus.ACTIVE,
                        PageRequest.of(0, normalizeLimit(limit))
                ).stream()
                .map(this::toLatestSale)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopProductDto> topProducts(int limit) {
        return saleDetailRepository.findTopProductsByCompanyId(currentUserService.getCompanyId(), PageRequest.of(0, normalizeLimit(limit)))
                .stream()
                .map(item -> TopProductDto.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantitySold(item.getQuantitySold())
                        .totalSold(item.getTotalSold())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SalesPeriodPointDto> salesPeriod(SalesPeriod period) {
        return switch (period) {
            case DAY -> dailySales(7);
            case WEEK -> weeklySales(4);
            case MONTH -> monthlySales(6);
        };
    }

    private List<SalesPeriodPointDto> dailySales(int days) {
        List<SalesPeriodPointDto> points = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);
            points.add(point(date.format(formatter), start, end));
        }
        return points;
    }

    private List<SalesPeriodPointDto> weeklySales(int weeks) {
        List<SalesPeriodPointDto> points = new ArrayList<>();
        for (int i = weeks - 1; i >= 0; i--) {
            LocalDate startDate = LocalDate.now().minusWeeks(i).minusDays(LocalDate.now().minusWeeks(i).getDayOfWeek().getValue() - 1L);
            LocalDate endDate = startDate.plusDays(6);
            points.add(point("Sem " + startDate.format(DateTimeFormatter.ofPattern("dd/MM")), startDate.atStartOfDay(), endDate.atTime(23, 59, 59)));
        }
        return points;
    }

    private List<SalesPeriodPointDto> monthlySales(int months) {
        List<SalesPeriodPointDto> points = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        for (int i = months - 1; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            points.add(point(month.format(formatter), month.atDay(1).atStartOfDay(), month.atEndOfMonth().atTime(23, 59, 59)));
        }
        return points;
    }

    private SalesPeriodPointDto point(String label, LocalDateTime start, LocalDateTime end) {
        Long companyId = currentUserService.getCompanyId();
        return SalesPeriodPointDto.builder()
                .label(label)
                .total(saleRepository.sumActiveSalesByCompanyIdAndDateBetween(companyId, start, end))
                .count(saleRepository.countByCompanyIdAndStatusAndSaleDateBetween(companyId, SaleStatus.ACTIVE, start, end))
                .build();
    }

    private LatestSaleDto toLatestSale(Sale sale) {
        return LatestSaleDto.builder()
                .id(sale.getId())
                .saleNumber(sale.getSaleNumber())
                .customerId(sale.getCustomer() != null ? sale.getCustomer().getId() : null)
                .customerName(sale.getCustomer() != null ? sale.getCustomer().getFullName() : null)
                .total(sale.getTotal())
                .status(sale.getStatus())
                .saleDate(sale.getSaleDate())
                .build();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 5;
        }
        return Math.min(limit, 20);
    }

    private Double calculateVariation(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
