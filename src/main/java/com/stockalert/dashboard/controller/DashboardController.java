package com.stockalert.dashboard.controller;

import com.stockalert.dashboard.dto.DashboardSummaryDto;
import com.stockalert.dashboard.service.DashboardService;
import com.stockalert.shared.response.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
