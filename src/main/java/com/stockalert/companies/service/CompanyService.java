package com.stockalert.companies.service;

import com.stockalert.companies.dto.CompanyAdminCreateDto;
import com.stockalert.companies.dto.CompanyCreateDto;
import com.stockalert.companies.dto.CompanyResponseDto;
import com.stockalert.companies.dto.CompanyUpdateDto;
import com.stockalert.companies.model.Company;
import com.stockalert.companies.model.CompanyStatus;
import com.stockalert.companies.repository.CompanyRepository;
import com.stockalert.security.CurrentUserService;
import com.stockalert.shared.exception.BusinessException;
import com.stockalert.shared.exception.NotFoundException;
import com.stockalert.shared.service.AuditService;
import com.stockalert.users.dto.UserResponseDto;
import com.stockalert.users.dto.PermissionResponseDto;
import com.stockalert.users.dto.RoleResponseDto;
import com.stockalert.users.model.Role;
import com.stockalert.users.model.User;
import com.stockalert.users.repository.RoleRepository;
import com.stockalert.users.repository.UserRepository;
import com.stockalert.users.service.DefaultRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private static final String COMPANY_ADMIN_ROLE = "ADMINISTRADOR";

    private final CompanyRepository companyRepository;
    private final AuditService auditService;
    private final DefaultRoleService defaultRoleService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<CompanyResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort sort = buildSort(sortBy, sortDirection);
        return companyRepository.findAll(PageRequest.of(page, size, sort)).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponseDto> findAll() {
        return companyRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponseDto findById(Long id) {
        return toResponse(findEntityById(id));
    }

    @Transactional
    public CompanyResponseDto create(CompanyCreateDto request) {
        Company company = Company.builder()
                .name(request.getName())
                .tradeName(request.getTradeName())
                .taxId(request.getTaxId())
                .verificationDigit(request.getVerificationDigit())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .department(request.getDepartment())
                .country(request.getCountry())
                .legalRepresentative(request.getLegalRepresentative())
                .legalRepresentativeDocument(request.getLegalRepresentativeDocument())
                .website(request.getWebsite())
                .logoPath(request.getLogoPath())
                .status(CompanyStatus.ACTIVE)
                .createdBy(auditService.getCurrentUsername())
                .build();
        Company savedCompany = companyRepository.save(company);
        defaultRoleService.createDefaultCompanyRoles(savedCompany, auditService.getCurrentUsername());
        return toResponse(savedCompany);
    }

    @Transactional
    public CompanyResponseDto update(Long id, CompanyUpdateDto request) {
        Company company = findEntityById(id);
        company.setName(request.getName());
        company.setTradeName(request.getTradeName());
        company.setTaxId(request.getTaxId());
        company.setVerificationDigit(request.getVerificationDigit());
        company.setEmail(request.getEmail());
        company.setPhone(request.getPhone());
        company.setAddress(request.getAddress());
        company.setCity(request.getCity());
        company.setDepartment(request.getDepartment());
        company.setCountry(request.getCountry());
        company.setLegalRepresentative(request.getLegalRepresentative());
        company.setLegalRepresentativeDocument(request.getLegalRepresentativeDocument());
        company.setWebsite(request.getWebsite());
        company.setLogoPath(request.getLogoPath());
        if (request.getStatus() != null) {
            company.setStatus(request.getStatus());
        }
        company.setUpdatedBy(auditService.getCurrentUsername());
        return toResponse(company);
    }

    @Transactional
    public void delete(Long id) {
        Company company = findEntityById(id);
        company.setStatus(CompanyStatus.INACTIVE);
        company.setDeletedAt(java.time.LocalDateTime.now());
        company.setUpdatedBy(auditService.getCurrentUsername());
    }

    @Transactional
    public CompanyResponseDto activate(Long id) {
        Company company = findEntityById(id);
        company.setStatus(CompanyStatus.ACTIVE);
        company.setDeletedAt(null);
        company.setUpdatedBy(auditService.getCurrentUsername());
        return toResponse(company);
    }

    @Transactional
    public CompanyResponseDto deactivate(Long id) {
        Company company = findEntityById(id);
        company.setStatus(CompanyStatus.INACTIVE);
        company.setUpdatedBy(auditService.getCurrentUsername());
        return toResponse(company);
    }

    @Transactional
    public UserResponseDto createCompanyAdmin(Long companyId, CompanyAdminCreateDto request) {
        Company company = findEntityById(companyId);
        if (company.getStatus() != CompanyStatus.ACTIVE) {
            throw new BusinessException("Solo se puede crear administrador para empresas activas");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Ya existe un usuario con username: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Ya existe un usuario con correo: " + request.getEmail());
        }

        defaultRoleService.createDefaultCompanyRoles(company, auditService.getCurrentUsername());
        Role adminRole = roleRepository.findByNameAndCompanyId(COMPANY_ADMIN_ROLE, company.getId())
                .orElseThrow(() -> new NotFoundException("Rol ADMINISTRADOR no encontrado para la empresa: " + company.getId()));

        if (userRepository.existsByCompanyIdAndRoles_Name(company.getId(), COMPANY_ADMIN_ROLE)) {
            throw new BusinessException("La empresa ya tiene un usuario administrador");
        }

        User user = User.builder()
                .company(company)
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .active(true)
                .createdBy(auditService.getCurrentUsername())
                .roles(Set.of(adminRole))
                .build();

        return toUserResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponseDto findCompanyAdmin(Long companyId) {
        currentUserService.validateSameCompanyOrSuperAdmin(companyId);
        findEntityById(companyId);
        User admin = userRepository.findFirstByCompanyIdAndRoles_Name(companyId, COMPANY_ADMIN_ROLE)
                .orElseThrow(() -> new NotFoundException("Administrador no encontrado para la empresa: " + companyId));
        return toUserResponse(admin);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> findCompanyUsers(Long companyId) {
        currentUserService.validateSameCompanyOrSuperAdmin(companyId);
        findEntityById(companyId);
        return userRepository.findByCompanyId(companyId).stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> findCompanyUsersPaginated(Long companyId, int page, int size, String sortBy, String sortDirection) {
        currentUserService.validateSameCompanyOrSuperAdmin(companyId);
        findEntityById(companyId);
        Sort sort = buildSort(sortBy, sortDirection);
        return userRepository.findByCompanyId(companyId, PageRequest.of(page, size, sort))
                .map(this::toUserResponse);
    }

    @Transactional(readOnly = true)
    public Company findEntityById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Empresa no encontrada con id: " + id));
    }

    public CompanyResponseDto toResponse(Company company) {
        return CompanyResponseDto.builder()
                .id(company.getId())
                .name(company.getName())
                .tradeName(company.getTradeName())
                .taxId(company.getTaxId())
                .verificationDigit(company.getVerificationDigit())
                .email(company.getEmail())
                .phone(company.getPhone())
                .address(company.getAddress())
                .city(company.getCity())
                .department(company.getDepartment())
                .country(company.getCountry())
                .legalRepresentative(company.getLegalRepresentative())
                .legalRepresentativeDocument(company.getLegalRepresentativeDocument())
                .website(company.getWebsite())
                .logoPath(company.getLogoPath())
                .status(company.getStatus())
                .statusLabel(company.getStatus().getLabel())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

    private UserResponseDto toUserResponse(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .companyId(user.getCompany().getId())
                .companyName(user.getCompany().getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles().stream().map(this::toRoleResponse).collect(Collectors.toSet()))
                .build();
    }

    private RoleResponseDto toRoleResponse(Role role) {
        return RoleResponseDto.builder()
                .id(role.getId())
                .companyId(role.getCompany().getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(role.getActive())
                .permissions(role.getPermissions().stream()
                        .map(permission -> PermissionResponseDto.builder()
                                .id(permission.getId())
                                .name(permission.getName())
                                .description(permission.getDescription())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }
}
