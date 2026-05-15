package com.stockalert.companies.service;

import com.stockalert.companies.dto.CompanyCreateDto;
import com.stockalert.companies.dto.CompanyResponseDto;
import com.stockalert.companies.dto.CompanyUpdateDto;
import com.stockalert.companies.model.Company;
import com.stockalert.companies.repository.CompanyRepository;
import com.stockalert.shared.exception.NotFoundException;
import com.stockalert.shared.service.AuditService;
import com.stockalert.users.service.DefaultRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final AuditService auditService;
    private final DefaultRoleService defaultRoleService;

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
                .taxId(request.getTaxId())
                .active(true)
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
        company.setTaxId(request.getTaxId());
        if (request.getActive() != null) {
            company.setActive(request.getActive());
        }
        company.setUpdatedBy(auditService.getCurrentUsername());
        return toResponse(company);
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
                .taxId(company.getTaxId())
                .active(company.getActive())
                .createdAt(company.getCreatedAt())
                .build();
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }
}
