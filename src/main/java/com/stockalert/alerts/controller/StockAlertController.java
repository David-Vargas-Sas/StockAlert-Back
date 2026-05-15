package com.stockalert.alerts.controller;

import com.stockalert.alerts.dto.StockAlertResponseDto;
import com.stockalert.alerts.service.StockAlertService;
import com.stockalert.shared.response.ApiResponseDto;
import com.stockalert.shared.response.PageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alertas", description = "Gestion de alertas de reposicion por empresa")
public class StockAlertController {

    private static final Logger logger = LoggerFactory.getLogger(StockAlertController.class);

    private final StockAlertService stockAlertService;

    public StockAlertController(StockAlertService stockAlertService) {
        this.stockAlertService = stockAlertService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ALERT_READ')")
    @Operation(summary = "Obtener todas las alertas")
    public ResponseEntity<ApiResponseDto<List<StockAlertResponseDto>>> findAll() {
        logger.info("Solicitud para obtener alertas");
        return ResponseEntity.ok(ApiResponseDto.success("Alertas obtenidas correctamente", stockAlertService.findAll()));
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAuthority('ALERT_READ')")
    @Operation(summary = "Obtener alertas paginadas")
    public ResponseEntity<ApiResponseDto<PageResponseDto<StockAlertResponseDto>>> findAllPaginated(
            @RequestParam(defaultValue = "0") @Parameter(description = "Numero de pagina") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Tamano de pagina") int size,
            @RequestParam(defaultValue = "id") @Parameter(description = "Campo por el que ordenar") String sortBy,
            @RequestParam(defaultValue = "asc") @Parameter(description = "Direccion de ordenamiento") String sortDirection) {
        logger.info("Solicitud paginada alertas - page: {}, size: {}, sortBy: {}, direction: {}", page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success(
                "Alertas paginadas obtenidas correctamente",
                PageResponseDto.from(stockAlertService.findAllPaginated(page, size, sortBy, sortDirection))
        ));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('ALERT_READ')")
    @Operation(summary = "Obtener alertas activas")
    public ResponseEntity<ApiResponseDto<List<StockAlertResponseDto>>> findActive() {
        logger.info("Solicitud para obtener alertas activas");
        return ResponseEntity.ok(ApiResponseDto.success("Alertas activas obtenidas correctamente", stockAlertService.findActive()));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('ALERT_RESOLVE')")
    @Operation(summary = "Resolver alerta")
    public ResponseEntity<ApiResponseDto<StockAlertResponseDto>> resolve(
            @PathVariable @Parameter(description = "ID de la alerta") Long id) {
        logger.info("Solicitud para resolver alerta id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Alerta resuelta correctamente", stockAlertService.resolve(id)));
    }
}
