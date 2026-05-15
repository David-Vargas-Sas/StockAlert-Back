package com.stockalert.auth.controller;

import com.stockalert.auth.dto.AuthResponseDto;
import com.stockalert.auth.dto.LoginRequestDto;
import com.stockalert.auth.dto.LogoutRequestDto;
import com.stockalert.auth.dto.LogoutResponseDto;
import com.stockalert.auth.dto.RefreshTokenRequestDto;
import com.stockalert.auth.service.AuthService;
import com.stockalert.shared.response.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Login y logout de usuarios")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> login(
            @Valid @RequestBody @Parameter(description = "Credenciales del usuario") LoginRequestDto request,
            HttpServletRequest httpRequest) {
        logger.info("Solicitud de login para usuario={}", request.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Login realizado correctamente", authService.login(request, httpRequest)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token de acceso")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> refresh(
            @Valid @RequestBody @Parameter(description = "Refresh token") RefreshTokenRequestDto request,
            HttpServletRequest httpRequest) {
        logger.info("Solicitud de refresh token");
        return ResponseEntity.ok(ApiResponseDto.success("Token renovado correctamente", authService.refresh(request, httpRequest)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesion")
    public ResponseEntity<ApiResponseDto<LogoutResponseDto>> logout(
            @RequestBody(required = false) LogoutRequestDto request,
            HttpServletRequest httpRequest) {
        logger.info("Solicitud de logout");
        return ResponseEntity.ok(ApiResponseDto.success("Logout realizado correctamente", authService.logout(request, httpRequest)));
    }
}
