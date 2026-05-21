package com.stockalert.suppliers.service;

import com.stockalert.companies.model.Company;
import com.stockalert.companies.service.CompanyService;
import com.stockalert.audit.service.AuditLogService;
import com.stockalert.security.CurrentUserService;
import com.stockalert.shared.exception.NotFoundException;
import com.stockalert.shared.service.AuditService;
import com.stockalert.suppliers.dto.SupplierCreateDto;
import com.stockalert.suppliers.dto.SupplierResponseDto;
import com.stockalert.suppliers.dto.SupplierUpdateDto;
import com.stockalert.suppliers.model.Supplier;
import com.stockalert.suppliers.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final CompanyService companyService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<SupplierResponseDto> findAll() {
        return supplierRepository.findByCompanyId(currentUserService.getCompanyId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return supplierRepository.findByCompanyId(currentUserService.getCompanyId(), PageRequest.of(page, size, Sort.by(direction, sortBy)))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public SupplierResponseDto findById(Long id) {
        return toResponse(findEntityByIdForCurrentCompany(id));
    }

    @Transactional
    public SupplierResponseDto create(SupplierCreateDto request) {
        Company company = companyService.findEntityById(currentUserService.getCompanyId());
        Supplier supplier = Supplier.builder()
                .company(company)
                .name(request.getName())
                .taxId(request.getTaxId())
                .contactName(request.getContactName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .active(true)
                .createdBy(auditService.getCurrentUsername())
                .build();
        Supplier saved = supplierRepository.save(supplier);
        auditLogService.record("CREATE", "Supplier", saved.getId(), "Proveedor creado: " + saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public SupplierResponseDto update(Long id, SupplierUpdateDto request) {
        Supplier supplier = findEntityByIdForCurrentCompany(id);
        supplier.setName(request.getName());
        supplier.setTaxId(request.getTaxId());
        supplier.setContactName(request.getContactName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        if (request.getActive() != null) {
            supplier.setActive(request.getActive());
        }
        supplier.setUpdatedBy(auditService.getCurrentUsername());
        auditLogService.record("UPDATE", "Supplier", supplier.getId(), "Proveedor actualizado: " + supplier.getName());
        return toResponse(supplier);
    }

    @Transactional
    public SupplierResponseDto activate(Long id) {
        Supplier supplier = findEntityByIdForCurrentCompany(id);
        supplier.setActive(true);
        supplier.setDeletedAt(null);
        supplier.setUpdatedBy(auditService.getCurrentUsername());
        auditLogService.record("ACTIVATE", "Supplier", supplier.getId(), "Proveedor activado: " + supplier.getName());
        return toResponse(supplier);
    }

    @Transactional
    public SupplierResponseDto deactivate(Long id) {
        Supplier supplier = findEntityByIdForCurrentCompany(id);
        supplier.setActive(false);
        supplier.setUpdatedBy(auditService.getCurrentUsername());
        auditLogService.record("DEACTIVATE", "Supplier", supplier.getId(), "Proveedor desactivado: " + supplier.getName());
        return toResponse(supplier);
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = findEntityByIdForCurrentCompany(id);
        supplier.setActive(false);
        supplier.setDeletedAt(LocalDateTime.now());
        supplier.setUpdatedBy(auditService.getCurrentUsername());
        auditLogService.record("DELETE", "Supplier", supplier.getId(), "Proveedor eliminado: " + supplier.getName());
    }

    @Transactional(readOnly = true)
    public Supplier findEntityByIdForCurrentCompany(Long id) {
        return supplierRepository.findByIdAndCompanyId(id, currentUserService.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado con id: " + id));
    }

    public SupplierResponseDto toResponse(Supplier supplier) {
        return SupplierResponseDto.builder()
                .id(supplier.getId())
                .companyId(supplier.getCompany().getId())
                .name(supplier.getName())
                .taxId(supplier.getTaxId())
                .contactName(supplier.getContactName())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .active(supplier.getActive())
                .createdAt(supplier.getCreatedAt())
                .build();
    }
}
