package com.stockalert.sales.controller;

import com.stockalert.sales.dto.SaleCreateDto;
import com.stockalert.sales.dto.SaleResponseDto;
import com.stockalert.sales.service.SaleService;
import com.stockalert.shared.response.ApiResponseDto;
import com.stockalert.shared.response.PageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@Tag(name = "Ventas", description = "Gestion de ventas por empresa")
public class SaleController {

    private static final Logger logger = LoggerFactory.getLogger(SaleController.class);

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SALE_READ')")
    @Operation(summary = "Obtener todas las ventas de la empresa autenticada")
    public ResponseEntity<ApiResponseDto<List<SaleResponseDto>>> findAll() {
        logger.info("Solicitud para obtener ventas");
        return ResponseEntity.ok(ApiResponseDto.success("Ventas obtenidas correctamente", saleService.findAll()));
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAuthority('SALE_READ')")
    @Operation(summary = "Obtener ventas paginadas")
    public ResponseEntity<ApiResponseDto<PageResponseDto<SaleResponseDto>>> findAllPaginated(
            @RequestParam(defaultValue = "0") @Parameter(description = "Numero de pagina") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Tamano de pagina") int size,
            @RequestParam(defaultValue = "id") @Parameter(description = "Campo por el que ordenar") String sortBy,
            @RequestParam(defaultValue = "asc") @Parameter(description = "Direccion de ordenamiento") String sortDirection) {
        logger.info("Solicitud paginada ventas - page: {}, size: {}, sortBy: {}, direction: {}", page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success(
                "Ventas paginadas obtenidas correctamente",
                PageResponseDto.from(saleService.findAllPaginated(page, size, sortBy, sortDirection))
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALE_READ')")
    @Operation(summary = "Obtener venta por ID")
    public ResponseEntity<ApiResponseDto<SaleResponseDto>> findById(
            @PathVariable @Parameter(description = "ID de la venta") Long id) {
        logger.info("Solicitud para obtener venta id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Venta obtenida correctamente", saleService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SALE_CREATE')")
    @Operation(summary = "Registrar venta")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venta registrada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o stock insuficiente")
    })
    public ResponseEntity<ApiResponseDto<SaleResponseDto>> create(
            @Valid @RequestBody @Parameter(description = "Datos de la venta") SaleCreateDto request) {
        logger.info("Solicitud para registrar venta");
        SaleResponseDto created = saleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Venta registrada correctamente", created));
    }
}
