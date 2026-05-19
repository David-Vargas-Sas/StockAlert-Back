package com.stockalert.users.service;

import com.stockalert.shared.exception.BusinessException;
import com.stockalert.shared.exception.NotFoundException;
import com.stockalert.users.dto.PermissionCreateDto;
import com.stockalert.users.dto.PermissionResponseDto;
import com.stockalert.users.dto.PermissionUpdateDto;
import com.stockalert.users.model.Permission;
import com.stockalert.users.repository.PermissionRepository;
import com.stockalert.users.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<PermissionResponseDto> findAll() {
        return permissionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public PermissionResponseDto create(PermissionCreateDto request) {
        Permission permission = Permission.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return toResponse(permissionRepository.save(permission));
    }

    @Transactional
    public PermissionResponseDto update(Long id, PermissionUpdateDto request) {
        Permission permission = findEntityById(id);
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        return toResponse(permission);
    }

    @Transactional
    public void delete(Long id) {
        Permission permission = findEntityById(id);
        if (roleRepository.existsByPermissions_Id(permission.getId())) {
            throw new BusinessException("No se puede eliminar el permiso porque esta asignado a roles");
        }
        permissionRepository.delete(permission);
    }

    @Transactional(readOnly = true)
    public Permission findEntityById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permiso no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public Set<Permission> findEntitiesByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        return ids.stream().map(this::findEntityById).collect(Collectors.toSet());
    }

    public PermissionResponseDto toResponse(Permission permission) {
        return PermissionResponseDto.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
    }
}
