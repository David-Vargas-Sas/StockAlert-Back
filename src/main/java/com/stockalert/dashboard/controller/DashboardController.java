package com.stockalert.dashboard.controller;

import com.stockalert.dashboard.dto.DashboardSummaryDto;
import com.stockalert.dashboard.dto.InventorySummaryDto;
import com.stockalert.dashboard.dto.LatestSaleDto;
import com.stockalert.dashboard.dto.SalesPeriod;
import com.stockalert.dashboard.dto.SalesPeriodPointDto;
import com.stockalert.dashboard.dto.TopProductDto;
import com.stockalert.dashboard.service.DashboardService;
import com.stockalert.shared.response.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Indicadores principales")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    @Operation(summary = "Obtener resumen del dashboard")
    public ResponseEntity<ApiResponseDto<DashboardSummaryDto>> summary() {
        return ResponseEntity.ok(ApiResponseDto.success("Resumen obtenido correctamente", dashboardService.summary()));
    }

    @GetMapping("/inventory-summary")
    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    @Operation(summary = "Obtener resumen de inventario")
    public ResponseEntity<ApiResponseDto<InventorySummaryDto>> inventorySummary() {
        return ResponseEntity.ok(ApiResponseDto.success("Resumen de inventario obtenido correctamente", dashboardService.inventorySummary()));
    }

    @GetMapping("/latest-sales")
    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    @Operation(summary = "Obtener ultimas ventas")
    public ResponseEntity<ApiResponseDto<List<LatestSaleDto>>> latestSales(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponseDto.success("Ultimas ventas obtenidas correctamente", dashboardService.latestSales(limit)));
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    @Operation(summary = "Obtener productos mas vendidos")
    public ResponseEntity<ApiResponseDto<List<TopProductDto>>> topProducts(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponseDto.success("Productos mas vendidos obtenidos correctamente", dashboardService.topProducts(limit)));
    }

    @GetMapping("/sales-period")
    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    @Operation(summary = "Obtener ventas por periodo")
    public ResponseEntity<ApiResponseDto<List<SalesPeriodPointDto>>> salesPeriod(
            @RequestParam(defaultValue = "DAY") SalesPeriod period) {
        return ResponseEntity.ok(ApiResponseDto.success("Ventas por periodo obtenidas correctamente", dashboardService.salesPeriod(period)));
    }
}
