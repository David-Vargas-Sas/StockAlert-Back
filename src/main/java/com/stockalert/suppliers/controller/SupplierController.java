package com.stockalert.suppliers.controller;

import com.stockalert.shared.response.ApiResponseDto;
import com.stockalert.shared.response.PageResponseDto;
import com.stockalert.suppliers.dto.SupplierCreateDto;
import com.stockalert.suppliers.dto.SupplierResponseDto;
import com.stockalert.suppliers.dto.SupplierUpdateDto;
import com.stockalert.suppliers.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@Tag(name = "Proveedores", description = "Gestion de proveedores por empresa")
public class SupplierController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    @Operation(summary = "Obtener proveedores")
    public ResponseEntity<ApiResponseDto<List<SupplierResponseDto>>> findAll() {
        logger.info("Solicitud para obtener proveedores");
        return ResponseEntity.ok(ApiResponseDto.success("Proveedores obtenidos correctamente", supplierService.findAll()));
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    @Operation(summary = "Obtener proveedores paginados")
    public ResponseEntity<ApiResponseDto<PageResponseDto<SupplierResponseDto>>> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        logger.info("Solicitud para obtener proveedores paginados - page: {}, size: {}, sortBy: {}, sortDirection: {}",
                page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success(
                "Proveedores paginados obtenidos correctamente",
                PageResponseDto.from(supplierService.findAllPaginated(page, size, sortBy, sortDirection))
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    @Operation(summary = "Obtener proveedor por ID")
    public ResponseEntity<ApiResponseDto<SupplierResponseDto>> findById(
            @PathVariable @Parameter(description = "ID del proveedor") Long id) {
        logger.info("Solicitud para obtener proveedor con ID: {}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Proveedor obtenido correctamente", supplierService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPPLIER_CREATE')")
    @Operation(summary = "Crear proveedor")
    public ResponseEntity<ApiResponseDto<SupplierResponseDto>> create(@Valid @RequestBody SupplierCreateDto request) {
        logger.info("Solicitud para crear proveedor");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Proveedor creado correctamente", supplierService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_UPDATE')")
    @Operation(summary = "Actualizar proveedor")
    public ResponseEntity<ApiResponseDto<SupplierResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierUpdateDto request) {
        logger.info("Solicitud para actualizar proveedor con ID: {}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Proveedor actualizado correctamente", supplierService.update(id, request)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('SUPPLIER_UPDATE')")
    @Operation(summary = "Activar proveedor")
    public ResponseEntity<ApiResponseDto<SupplierResponseDto>> activate(@PathVariable Long id) {
        logger.info("Solicitud para activar proveedor con ID: {}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Proveedor activado correctamente", supplierService.activate(id)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('SUPPLIER_UPDATE')")
    @Operation(summary = "Desactivar proveedor")
    public ResponseEntity<ApiResponseDto<SupplierResponseDto>> deactivate(@PathVariable Long id) {
        logger.info("Solicitud para desactivar proveedor con ID: {}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Proveedor desactivado correctamente", supplierService.deactivate(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_DELETE')")
    @Operation(summary = "Eliminar proveedor")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable Long id) {
        logger.info("Solicitud para eliminar proveedor con ID: {}", id);
        supplierService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Proveedor eliminado correctamente", null));
    }
}
