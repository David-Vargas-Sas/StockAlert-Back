package com.stockalert.users.service;

import com.stockalert.companies.model.Company;
import com.stockalert.companies.service.CompanyService;
import com.stockalert.security.CurrentUserService;
import com.stockalert.shared.exception.BusinessException;
import com.stockalert.shared.exception.NotFoundException;
import com.stockalert.shared.service.AuditService;
import com.stockalert.users.dto.RoleCreateDto;
import com.stockalert.users.dto.RoleResponseDto;
import com.stockalert.users.dto.RoleUpdateDto;
import com.stockalert.users.model.Role;
import com.stockalert.users.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final CompanyService companyService;
    private final PermissionService permissionService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final com.stockalert.users.repository.UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<RoleResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort sort = buildSort(sortBy, sortDirection);
        if (currentUserService.hasRole("SUPER_ADMIN")) {
            return roleRepository.findAll(PageRequest.of(page, size, sort)).map(this::toResponse);
        }
        return roleRepository.findByCompanyId(currentUserService.getCompanyId(), PageRequest.of(page, size, sort))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<RoleResponseDto> findAllForCurrentCompany() {
        if (currentUserService.hasRole("SUPER_ADMIN")) {
            return roleRepository.findAll().stream().map(this::toResponse).toList();
        }
        Long companyId = currentUserService.getCompanyId();
        return roleRepository.findByCompanyId(companyId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public RoleResponseDto create(RoleCreateDto request) {
        Long companyId = resolveTargetCompanyId(request.getCompanyId());
        Company company = companyService.findEntityById(companyId);
        Role role = Role.builder()
                .company(company)
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .createdBy(auditService.getCurrentUsername())
                .permissions(permissionService.findEntitiesByIds(request.getPermissionIds()))
                .build();
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public RoleResponseDto update(Long id, RoleUpdateDto request) {
        Role role = currentUserService.hasRole("SUPER_ADMIN")
                ? roleRepository.findById(id).orElseThrow(() -> new NotFoundException("Rol no encontrado con id: " + id))
                : findEntityByIdForCurrentCompany(id);
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        if (request.getActive() != null) {
            role.setActive(request.getActive());
        }
        role.setUpdatedBy(auditService.getCurrentUsername());
        role.setPermissions(permissionService.findEntitiesByIds(request.getPermissionIds()));
        return toResponse(role);
    }

    @Transactional
    public RoleResponseDto activate(Long id) {
        Role role = currentUserService.hasRole("SUPER_ADMIN")
                ? roleRepository.findById(id).orElseThrow(() -> new NotFoundException("Rol no encontrado con id: " + id))
                : findEntityByIdForCurrentCompany(id);
        role.setActive(true);
        role.setDeletedAt(null);
        role.setUpdatedBy(auditService.getCurrentUsername());
        return toResponse(role);
    }

    @Transactional
    public RoleResponseDto deactivate(Long id) {
        Role role = currentUserService.hasRole("SUPER_ADMIN")
                ? roleRepository.findById(id).orElseThrow(() -> new NotFoundException("Rol no encontrado con id: " + id))
                : findEntityByIdForCurrentCompany(id);
        if (isBaseRole(role.getName())) {
            throw new BusinessException("No se pueden desactivar roles base del sistema");
        }
        role.setActive(false);
        role.setUpdatedBy(auditService.getCurrentUsername());
        return toResponse(role);
    }

    @Transactional
    public void delete(Long id) {
        Role role = currentUserService.hasRole("SUPER_ADMIN")
                ? roleRepository.findById(id).orElseThrow(() -> new NotFoundException("Rol no encontrado con id: " + id))
                : findEntityByIdForCurrentCompany(id);
        if (isBaseRole(role.getName())) {
            throw new BusinessException("No se pueden eliminar roles base del sistema");
        }
        if (userRepository.existsByRoles_Id(role.getId())) {
            throw new BusinessException("No se puede eliminar el rol porque esta asignado a usuarios");
        }
        roleRepository.delete(role);
    }

    @Transactional(readOnly = true)
    public Role findEntityByIdForCurrentCompany(Long id) {
        return findEntityByIdAndCompanyId(id, currentUserService.getCompanyId());
    }

    @Transactional(readOnly = true)
    public Role findEntityByIdAndCompanyId(Long id, Long companyId) {
        return roleRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("Rol no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public Set<Role> findEntitiesByIdsAndCompanyId(Set<Long> ids, Long companyId) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        return ids.stream()
                .map(id -> findEntityByIdAndCompanyId(id, companyId))
                .collect(Collectors.toSet());
    }

    public RoleResponseDto toResponse(Role role) {
        return RoleResponseDto.builder()
                .id(role.getId())
                .companyId(role.getCompany().getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(role.getActive())
                .permissions(role.getPermissions().stream().map(permissionService::toResponse).collect(Collectors.toSet()))
                .build();
    }

    private Long resolveTargetCompanyId(Long requestedCompanyId) {
        if (currentUserService.hasRole("SUPER_ADMIN")) {
            return requestedCompanyId != null ? requestedCompanyId : currentUserService.getCompanyId();
        }
        if (requestedCompanyId != null) {
            currentUserService.validateSameCompanyOrSuperAdmin(requestedCompanyId);
        }
        return currentUserService.getCompanyId();
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }

    private boolean isBaseRole(String roleName) {
        return Set.of("SUPER_ADMIN", "ADMINISTRADOR", "VENDEDOR", "BODEGUERO", "CONSULTOR").contains(roleName);
    }
}
