package com.stockalert.audit.service;

import com.stockalert.audit.dto.AuditLogResponseDto;
import com.stockalert.audit.model.AuditLog;
import com.stockalert.audit.repository.AuditLogRepository;
import com.stockalert.companies.model.Company;
import com.stockalert.companies.service.CompanyService;
import com.stockalert.security.CurrentUserService;
import com.stockalert.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final CompanyService companyService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return auditLogRepository.findByCompanyId(currentUserService.getCompanyId(), PageRequest.of(page, size, Sort.by(direction, sortBy)))
                .map(this::toResponse);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String entityName, Long entityId, String description) {
        Company company = companyService.findEntityById(currentUserService.getCompanyId());
        auditLogRepository.save(AuditLog.builder()
                .company(company)
                .username(auditService.getCurrentUsername())
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .description(description)
                .build());
    }

    private AuditLogResponseDto toResponse(AuditLog auditLog) {
        return AuditLogResponseDto.builder()
                .id(auditLog.getId())
                .companyId(auditLog.getCompany().getId())
                .username(auditLog.getUsername())
                .action(auditLog.getAction())
                .entityName(auditLog.getEntityName())
                .entityId(auditLog.getEntityId())
                .description(auditLog.getDescription())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
