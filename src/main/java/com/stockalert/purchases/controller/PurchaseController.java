package com.stockalert.purchases.controller;

import com.stockalert.purchases.dto.PurchaseCreateDto;
import com.stockalert.purchases.dto.PurchaseResponseDto;
import com.stockalert.purchases.service.PurchaseService;
import com.stockalert.shared.response.ApiResponseDto;
import com.stockalert.shared.response.PageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@Tag(name = "Compras", description = "Gestion de compras y reposicion")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PURCHASE_READ')")
    @Operation(summary = "Obtener compras")
    public ResponseEntity<ApiResponseDto<List<PurchaseResponseDto>>> findAll() {
        return ResponseEntity.ok(ApiResponseDto.success("Compras obtenidas correctamente", purchaseService.findAll()));
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAuthority('PURCHASE_READ')")
    @Operation(summary = "Obtener compras paginadas")
    public ResponseEntity<ApiResponseDto<PageResponseDto<PurchaseResponseDto>>> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(ApiResponseDto.success(
                "Compras paginadas obtenidas correctamente",
                PageResponseDto.from(purchaseService.findAllPaginated(page, size, sortBy, sortDirection))
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_READ')")
    @Operation(summary = "Obtener compra por ID")
    public ResponseEntity<ApiResponseDto<PurchaseResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDto.success("Compra obtenida correctamente", purchaseService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PURCHASE_CREATE')")
    @Operation(summary = "Registrar compra")
    public ResponseEntity<ApiResponseDto<PurchaseResponseDto>> create(@Valid @RequestBody PurchaseCreateDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Compra registrada correctamente", purchaseService.create(request)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PURCHASE_CANCEL')")
    @Operation(summary = "Anular compra")
    public ResponseEntity<ApiResponseDto<PurchaseResponseDto>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDto.success("Compra anulada correctamente", purchaseService.cancel(id)));
    }
}
