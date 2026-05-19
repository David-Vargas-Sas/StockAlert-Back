package com.stockalert.users.controller;

import com.stockalert.users.dto.RoleCreateDto;
import com.stockalert.users.dto.RoleResponseDto;
import com.stockalert.users.dto.RoleUpdateDto;
import com.stockalert.users.service.RoleService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Gestion de roles por empresa")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ')")
    @Operation(summary = "Obtener roles de la empresa autenticada")
    public ResponseEntity<ApiResponseDto<List<RoleResponseDto>>> findAll() {
        logger.info("Solicitud para obtener roles");
        return ResponseEntity.ok(ApiResponseDto.success("Roles obtenidos correctamente", roleService.findAllForCurrentCompany()));
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    @Operation(summary = "Obtener roles paginados")
    public ResponseEntity<ApiResponseDto<PageResponseDto<RoleResponseDto>>> findAllPaginated(
            @RequestParam(defaultValue = "0") @Parameter(description = "Numero de pagina") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Tamano de pagina") int size,
            @RequestParam(defaultValue = "id") @Parameter(description = "Campo por el que ordenar") String sortBy,
            @RequestParam(defaultValue = "asc") @Parameter(description = "Direccion de ordenamiento") String sortDirection) {
        logger.info("Solicitud paginada roles - page: {}, size: {}, sortBy: {}, direction: {}", page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success(
                "Roles paginados obtenidos correctamente",
                PageResponseDto.from(roleService.findAllPaginated(page, size, sortBy, sortDirection))
        ));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    @Operation(summary = "Crear rol")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> create(
            @Valid @RequestBody @Parameter(description = "Datos del rol") RoleCreateDto request) {
        logger.info("Solicitud para crear rol");
        RoleResponseDto created = roleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Rol creado correctamente", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @Operation(summary = "Actualizar rol")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> update(
            @PathVariable @Parameter(description = "ID del rol") Long id,
            @Valid @RequestBody RoleUpdateDto request) {
        logger.info("Solicitud para actualizar rol id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Rol actualizado correctamente", roleService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    @Operation(summary = "Eliminar rol")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable @Parameter(description = "ID del rol") Long id) {
        logger.info("Solicitud para eliminar rol id={}", id);
        roleService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Rol eliminado correctamente", null));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @Operation(summary = "Activar rol")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> activate(
            @PathVariable @Parameter(description = "ID del rol") Long id) {
        logger.info("Solicitud para activar rol id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Rol activado correctamente", roleService.activate(id)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @Operation(summary = "Desactivar rol")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> deactivate(
            @PathVariable @Parameter(description = "ID del rol") Long id) {
        logger.info("Solicitud para desactivar rol id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Rol desactivado correctamente", roleService.deactivate(id)));
    }
}
