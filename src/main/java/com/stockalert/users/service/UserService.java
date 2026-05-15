package com.stockalert.users.service;

import com.stockalert.companies.model.Company;
import com.stockalert.companies.service.CompanyService;
import com.stockalert.security.CurrentUserService;
import com.stockalert.shared.exception.BusinessException;
import com.stockalert.shared.exception.NotFoundException;
import com.stockalert.shared.service.AuditService;
import com.stockalert.users.dto.UserCreateDto;
import com.stockalert.users.dto.UserResponseDto;
import com.stockalert.users.dto.UserUpdateDto;
import com.stockalert.users.model.User;
import com.stockalert.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CompanyService companyService;
    private final RoleService roleService;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort sort = buildSort(sortBy, sortDirection);
        if (currentUserService.hasRole("SUPER_ADMIN")) {
            return userRepository.findAll(PageRequest.of(page, size, sort)).map(this::toResponse);
        }
        return userRepository.findByCompanyId(currentUserService.getCompanyId(), PageRequest.of(page, size, sort))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> findAllForCurrentCompany() {
        if (currentUserService.hasRole("SUPER_ADMIN")) {
            return userRepository.findAll().stream()
                    .map(this::toResponse)
                    .toList();
        }
        return userRepository.findByCompanyId(currentUserService.getCompanyId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserResponseDto create(UserCreateDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Ya existe un usuario con username: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Ya existe un usuario con correo: " + request.getEmail());
        }

        Long companyId = resolveTargetCompanyId(request.getCompanyId());
        Company company = companyService.findEntityById(companyId);
        User user = User.builder()
                .company(company)
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .active(true)
                .createdBy(auditService.getCurrentUsername())
                .roles(roleService.findEntitiesByIdsAndCompanyId(request.getRoleIds(), company.getId()))
                .build();
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDto update(Long id, UserUpdateDto request) {
        User user = currentUserService.hasRole("SUPER_ADMIN")
                ? userRepository.findById(id).orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + id))
                : findEntityByIdForCurrentCompany(id);
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        user.setUpdatedBy(auditService.getCurrentUsername());
        user.setRoles(roleService.findEntitiesByIdsAndCompanyId(request.getRoleIds(), user.getCompany().getId()));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public User findEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + username));
    }

    @Transactional(readOnly = true)
    public User findEntityByIdForCurrentCompany(Long id) {
        return userRepository.findByIdAndCompanyId(id, currentUserService.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + id));
    }

    public UserResponseDto toResponse(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .companyId(user.getCompany().getId())
                .companyName(user.getCompany().getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles().stream().map(roleService::toResponse).collect(Collectors.toSet()))
                .build();
    }

    private Long resolveTargetCompanyId(Long requestedCompanyId) {
        if (currentUserService.hasRole("SUPER_ADMIN")) {
            if (requestedCompanyId == null) {
                throw new BusinessException("El id de la empresa es obligatorio para crear usuarios como SUPER_ADMIN");
            }
            return requestedCompanyId;
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
}
