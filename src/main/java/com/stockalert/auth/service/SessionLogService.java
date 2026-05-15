package com.stockalert.auth.service;

import com.stockalert.auth.dto.SessionLogResponseDto;
import com.stockalert.auth.model.SessionLog;
import com.stockalert.auth.repository.SessionLogRepository;
import com.stockalert.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionLogService {

    private final SessionLogRepository sessionLogRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<SessionLogResponseDto> findAllPaginated(
            Long companyId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = buildSort(sortBy, sortDirection);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        if (currentUserService.hasRole("SUPER_ADMIN")) {
            if (companyId != null) {
                return sessionLogRepository.findByCompanyId(companyId, pageRequest).map(this::toResponse);
            }
            return sessionLogRepository.findAll(pageRequest).map(this::toResponse);
        }

        Long currentCompanyId = currentUserService.getCompanyId();
        if (companyId != null) {
            currentUserService.validateSameCompanyOrSuperAdmin(companyId);
        }
        return sessionLogRepository.findByCompanyId(currentCompanyId, pageRequest).map(this::toResponse);
    }

    private SessionLogResponseDto toResponse(SessionLog sessionLog) {
        return SessionLogResponseDto.builder()
                .id(sessionLog.getId())
                .userId(sessionLog.getUser() != null ? sessionLog.getUser().getId() : null)
                .companyId(sessionLog.getCompany() != null ? sessionLog.getCompany().getId() : null)
                .companyName(sessionLog.getCompany() != null ? sessionLog.getCompany().getName() : null)
                .username(sessionLog.getUsername())
                .eventType(sessionLog.getEventType())
                .ipAddress(sessionLog.getIpAddress())
                .message(sessionLog.getMessage())
                .createdAt(sessionLog.getCreatedAt())
                .build();
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, sortBy);
    }
}
