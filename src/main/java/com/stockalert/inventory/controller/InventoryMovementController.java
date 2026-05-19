package com.stockalert.inventory.controller;

import com.stockalert.inventory.dto.InventoryAdjustmentDto;
import com.stockalert.inventory.dto.InventoryMovementResponseDto;
import com.stockalert.inventory.service.InventoryMovementService;
import com.stockalert.shared.response.ApiResponseDto;
import com.stockalert.shared.response.PageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory-movements")
@Tag(name = "Movimientos de inventario", description = "Trazabilidad del stock")
public class InventoryMovementController {

    private final InventoryMovementService inventoryMovementService;

    public InventoryMovementController(InventoryMovementService inventoryMovementService) {
        this.inventoryMovementService = inventoryMovementService;
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @Operation(summary = "Obtener movimientos de inventario paginados")
    public ResponseEntity<ApiResponseDto<PageResponseDto<InventoryMovementResponseDto>>> findAllPaginated(
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(ApiResponseDto.success(
                "Movimientos de inventario obtenidos correctamente",
                PageResponseDto.from(inventoryMovementService.findAllPaginated(productId, page, size, sortBy, sortDirection))
        ));
    }

    @PatchMapping("/adjustment-in")
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    @Operation(summary = "Registrar ajuste de entrada")
    public ResponseEntity<ApiResponseDto<InventoryMovementResponseDto>> adjustmentIn(@Valid @RequestBody InventoryAdjustmentDto request) {
        return ResponseEntity.ok(ApiResponseDto.success("Ajuste de entrada registrado correctamente", inventoryMovementService.adjustmentIn(request)));
    }

    @PatchMapping("/adjustment-out")
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    @Operation(summary = "Registrar ajuste de salida")
    public ResponseEntity<ApiResponseDto<InventoryMovementResponseDto>> adjustmentOut(@Valid @RequestBody InventoryAdjustmentDto request) {
        return ResponseEntity.ok(ApiResponseDto.success("Ajuste de salida registrado correctamente", inventoryMovementService.adjustmentOut(request)));
    }
}
