package com.stockalert.users.controller;

import com.stockalert.users.dto.UserCreateDto;
import com.stockalert.users.dto.UserResponseDto;
import com.stockalert.users.dto.UserUpdateDto;
import com.stockalert.users.dto.ChangePasswordDto;
import com.stockalert.users.dto.ResetPasswordDto;
import com.stockalert.users.service.UserService;
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
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Gestion de usuarios por empresa")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    @Operation(summary = "Obtener usuarios de la empresa autenticada")
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> findAll() {
        logger.info("Solicitud para obtener usuarios");
        return ResponseEntity.ok(ApiResponseDto.success("Usuarios obtenidos correctamente", userService.findAllForCurrentCompany()));
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAuthority('USER_READ')")
    @Operation(summary = "Obtener usuarios paginados")
    public ResponseEntity<ApiResponseDto<PageResponseDto<UserResponseDto>>> findAllPaginated(
            @RequestParam(defaultValue = "0") @Parameter(description = "Numero de pagina") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Tamano de pagina") int size,
            @RequestParam(defaultValue = "id") @Parameter(description = "Campo por el que ordenar") String sortBy,
            @RequestParam(defaultValue = "asc") @Parameter(description = "Direccion de ordenamiento") String sortDirection) {
        logger.info("Solicitud paginada usuarios - page: {}, size: {}, sortBy: {}, direction: {}", page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success(
                "Usuarios paginados obtenidos correctamente",
                PageResponseDto.from(userService.findAllPaginated(page, size, sortBy, sortDirection))
        ));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    @Operation(summary = "Crear usuario")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> create(
            @Valid @RequestBody @Parameter(description = "Datos del usuario") UserCreateDto request) {
        logger.info("Solicitud para crear usuario username={}", request.getUsername());
        UserResponseDto created = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Usuario creado correctamente", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> update(
            @PathVariable @Parameter(description = "ID del usuario") Long id,
            @Valid @RequestBody UserUpdateDto request) {
        logger.info("Solicitud para actualizar usuario id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Usuario actualizado correctamente", userService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable @Parameter(description = "ID del usuario") Long id) {
        logger.info("Solicitud para eliminar usuario id={}", id);
        userService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Usuario eliminado correctamente", null));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @Operation(summary = "Activar usuario")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> activate(
            @PathVariable @Parameter(description = "ID del usuario") Long id) {
        logger.info("Solicitud para activar usuario id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Usuario activado correctamente", userService.activate(id)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @Operation(summary = "Desactivar usuario")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> deactivate(
            @PathVariable @Parameter(description = "ID del usuario") Long id) {
        logger.info("Solicitud para desactivar usuario id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Usuario desactivado correctamente", userService.deactivate(id)));
    }

    @PatchMapping("/me/password")
    @Operation(summary = "Cambiar mi contrasena")
    public ResponseEntity<ApiResponseDto<Void>> changeOwnPassword(@Valid @RequestBody ChangePasswordDto request) {
        logger.info("Solicitud para cambiar contrasena propia");
        userService.changeOwnPassword(request);
        return ResponseEntity.ok(ApiResponseDto.success("Contrasena actualizada correctamente", null));
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @Operation(summary = "Restablecer contrasena de usuario")
    public ResponseEntity<ApiResponseDto<Void>> resetPassword(
            @PathVariable @Parameter(description = "ID del usuario") Long id,
            @Valid @RequestBody ResetPasswordDto request) {
        logger.info("Solicitud para restablecer contrasena de usuario id={}", id);
        userService.resetPassword(id, request);
        return ResponseEntity.ok(ApiResponseDto.success("Contrasena restablecida correctamente", null));
    }
}
