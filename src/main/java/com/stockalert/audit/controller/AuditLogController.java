package com.stockalert.audit.controller;

import com.stockalert.audit.dto.AuditLogResponseDto;
import com.stockalert.audit.service.AuditLogService;
import com.stockalert.shared.response.ApiResponseDto;
import com.stockalert.shared.response.PageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Auditoria", description = "Logs generales de auditoria")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAuthority('AUDIT_LOG_READ')")
    @Operation(summary = "Obtener auditoria paginada")
    public ResponseEntity<ApiResponseDto<PageResponseDto<AuditLogResponseDto>>> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(ApiResponseDto.success(
                "Logs de auditoria obtenidos correctamente",
                PageResponseDto.from(auditLogService.findAllPaginated(page, size, sortBy, sortDirection))
        ));
    }
}
