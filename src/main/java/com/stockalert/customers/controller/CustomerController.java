package com.stockalert.customers.controller;

import com.stockalert.customers.dto.CustomerCreateDto;
import com.stockalert.customers.dto.CustomerResponseDto;
import com.stockalert.customers.dto.CustomerUpdateDto;
import com.stockalert.customers.service.CustomerService;
import com.stockalert.shared.response.ApiResponseDto;
import com.stockalert.shared.response.PageResponseDto;
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
@RequestMapping("/api/customers")
@Tag(name = "Clientes", description = "Gestion de clientes por empresa")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    @Operation(summary = "Obtener clientes")
    public ResponseEntity<ApiResponseDto<List<CustomerResponseDto>>> findAll() {
        logger.info("Solicitud para obtener clientes");
        return ResponseEntity.ok(ApiResponseDto.success("Clientes obtenidos correctamente", customerService.findAll()));
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    @Operation(summary = "Obtener clientes paginados")
    public ResponseEntity<ApiResponseDto<PageResponseDto<CustomerResponseDto>>> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(ApiResponseDto.success(
                "Clientes paginados obtenidos correctamente",
                PageResponseDto.from(customerService.findAllPaginated(page, size, sortBy, sortDirection))
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    @Operation(summary = "Obtener cliente por ID")
    public ResponseEntity<ApiResponseDto<CustomerResponseDto>> findById(
            @PathVariable @Parameter(description = "ID del cliente") Long id) {
        return ResponseEntity.ok(ApiResponseDto.success("Cliente obtenido correctamente", customerService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER_CREATE')")
    @Operation(summary = "Crear cliente")
    public ResponseEntity<ApiResponseDto<CustomerResponseDto>> create(@Valid @RequestBody CustomerCreateDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Cliente creado correctamente", customerService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_UPDATE')")
    @Operation(summary = "Actualizar cliente")
    public ResponseEntity<ApiResponseDto<CustomerResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateDto request) {
        return ResponseEntity.ok(ApiResponseDto.success("Cliente actualizado correctamente", customerService.update(id, request)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('CUSTOMER_UPDATE')")
    @Operation(summary = "Activar cliente")
    public ResponseEntity<ApiResponseDto<CustomerResponseDto>> activate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDto.success("Cliente activado correctamente", customerService.activate(id)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('CUSTOMER_UPDATE')")
    @Operation(summary = "Desactivar cliente")
    public ResponseEntity<ApiResponseDto<CustomerResponseDto>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDto.success("Cliente desactivado correctamente", customerService.deactivate(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_DELETE')")
    @Operation(summary = "Eliminar cliente")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Cliente eliminado correctamente", null));
    }
}
