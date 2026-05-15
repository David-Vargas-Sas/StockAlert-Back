package com.stockalert.auth.controller;

import com.stockalert.auth.dto.SessionLogResponseDto;
import com.stockalert.auth.service.SessionLogService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session-logs")
@Tag(name = "Logs de sesion", description = "Auditoria de sesiones de usuarios")
public class SessionLogController {

    private static final Logger logger = LoggerFactory.getLogger(SessionLogController.class);

    private final SessionLogService sessionLogService;

    public SessionLogController(SessionLogService sessionLogService) {
        this.sessionLogService = sessionLogService;
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAuthority('SESSION_LOG_READ')")
    @Operation(summary = "Obtener logs de sesion paginados")
    public ResponseEntity<ApiResponseDto<PageResponseDto<SessionLogResponseDto>>> findAllPaginated(
            @RequestParam(required = false) @Parameter(description = "ID de empresa para filtrar") Long companyId,
            @RequestParam(defaultValue = "0") @Parameter(description = "Numero de pagina") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Tamano de pagina") int size,
            @RequestParam(defaultValue = "createdAt") @Parameter(description = "Campo por el que ordenar") String sortBy,
            @RequestParam(defaultValue = "desc") @Parameter(description = "Direccion de ordenamiento") String sortDirection) {

        logger.info("Solicitud paginada logs de sesion - companyId: {}, page: {}, size: {}, sortBy: {}, direction: {}",
                companyId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success(
                "Logs de sesion paginados obtenidos correctamente",
                PageResponseDto.from(sessionLogService.findAllPaginated(companyId, page, size, sortBy, sortDirection))
        ));
    }
}
