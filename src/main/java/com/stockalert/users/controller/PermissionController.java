package com.stockalert.users.controller;

import com.stockalert.users.dto.PermissionCreateDto;
import com.stockalert.users.dto.PermissionResponseDto;
import com.stockalert.users.dto.PermissionUpdateDto;
import com.stockalert.users.service.PermissionService;
import com.stockalert.shared.response.ApiResponseDto;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@Tag(name = "Permisos", description = "Gestion de permisos")
public class PermissionController {

    private static final Logger logger = LoggerFactory.getLogger(PermissionController.class);

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    @Operation(summary = "Obtener permisos")
    public ResponseEntity<ApiResponseDto<List<PermissionResponseDto>>> findAll() {
        logger.info("Solicitud para obtener permisos");
        return ResponseEntity.ok(ApiResponseDto.success("Permisos obtenidos correctamente", permissionService.findAll()));
    }

    @GetMapping("/available")
    @PreAuthorize("hasAuthority('ROLE_READ') or hasAuthority('ROLE_CREATE') or hasAuthority('ROLE_UPDATE')")
    @Operation(summary = "Obtener permisos disponibles para roles")
    public ResponseEntity<ApiResponseDto<List<PermissionResponseDto>>> findAvailableForRoles() {
        logger.info("Solicitud para obtener permisos disponibles para roles");
        return ResponseEntity.ok(ApiResponseDto.success(
                "Permisos disponibles obtenidos correctamente",
                permissionService.findAll()
        ));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    @Operation(summary = "Crear permiso")
    public ResponseEntity<ApiResponseDto<PermissionResponseDto>> create(
            @Valid @RequestBody @Parameter(description = "Datos del permiso") PermissionCreateDto request) {
        logger.info("Solicitud para crear permiso");
        PermissionResponseDto created = permissionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Permiso creado correctamente", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
    @Operation(summary = "Actualizar permiso")
    public ResponseEntity<ApiResponseDto<PermissionResponseDto>> update(
            @PathVariable @Parameter(description = "ID del permiso") Long id,
            @Valid @RequestBody PermissionUpdateDto request) {
        logger.info("Solicitud para actualizar permiso id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Permiso actualizado correctamente", permissionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    @Operation(summary = "Eliminar permiso")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable @Parameter(description = "ID del permiso") Long id) {
        logger.info("Solicitud para eliminar permiso id={}", id);
        permissionService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Permiso eliminado correctamente", null));
    }
}
