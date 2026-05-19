package com.stockalert.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDto {

    private Long id;
    private Long companyId;
    private String username;
    private String action;
    private String entityName;
    private Long entityId;
    private String description;
    private LocalDateTime createdAt;
}
