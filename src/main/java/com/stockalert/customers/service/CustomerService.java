package com.stockalert.customers.service;

import com.stockalert.companies.model.Company;
import com.stockalert.companies.service.CompanyService;
import com.stockalert.audit.service.AuditLogService;
import com.stockalert.customers.dto.CustomerCreateDto;
import com.stockalert.customers.dto.CustomerResponseDto;
import com.stockalert.customers.dto.CustomerUpdateDto;
import com.stockalert.customers.model.Customer;
import com.stockalert.customers.repository.CustomerRepository;
import com.stockalert.security.CurrentUserService;
import com.stockalert.shared.exception.NotFoundException;
import com.stockalert.shared.service.AuditService;
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
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CompanyService companyService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<CustomerResponseDto> findAll() {
        return customerRepository.findByCompanyId(currentUserService.getCompanyId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return customerRepository.findByCompanyId(currentUserService.getCompanyId(), PageRequest.of(page, size, Sort.by(direction, sortBy)))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponseDto findById(Long id) {
        return toResponse(findEntityByIdForCurrentCompany(id));
    }

    @Transactional
    public CustomerResponseDto create(CustomerCreateDto request) {
        Company company = companyService.findEntityById(currentUserService.getCompanyId());
        Customer customer = Customer.builder()
                .company(company)
                .fullName(request.getFullName())
                .documentNumber(request.getDocumentNumber())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .active(true)
                .createdBy(auditService.getCurrentUsername())
                .build();
        Customer saved = customerRepository.save(customer);
        auditLogService.record("CREATE", "Customer", saved.getId(), "Cliente creado: " + saved.getFullName());
        return toResponse(saved);
    }

    @Transactional
    public CustomerResponseDto update(Long id, CustomerUpdateDto request) {
        Customer customer = findEntityByIdForCurrentCompany(id);
        customer.setFullName(request.getFullName());
        customer.setDocumentNumber(request.getDocumentNumber());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        if (request.getActive() != null) {
            customer.setActive(request.getActive());
        }
        customer.setUpdatedBy(auditService.getCurrentUsername());
        auditLogService.record("UPDATE", "Customer", customer.getId(), "Cliente actualizado: " + customer.getFullName());
        return toResponse(customer);
    }

    @Transactional
    public CustomerResponseDto activate(Long id) {
        Customer customer = findEntityByIdForCurrentCompany(id);
        customer.setActive(true);
        customer.setDeletedAt(null);
        customer.setUpdatedBy(auditService.getCurrentUsername());
        auditLogService.record("ACTIVATE", "Customer", customer.getId(), "Cliente activado: " + customer.getFullName());
        return toResponse(customer);
    }

    @Transactional
    public CustomerResponseDto deactivate(Long id) {
        Customer customer = findEntityByIdForCurrentCompany(id);
        customer.setActive(false);
        customer.setUpdatedBy(auditService.getCurrentUsername());
        auditLogService.record("DEACTIVATE", "Customer", customer.getId(), "Cliente desactivado: " + customer.getFullName());
        return toResponse(customer);
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = findEntityByIdForCurrentCompany(id);
        customer.setActive(false);
        customer.setDeletedAt(LocalDateTime.now());
        customer.setUpdatedBy(auditService.getCurrentUsername());
        auditLogService.record("DELETE", "Customer", customer.getId(), "Cliente eliminado: " + customer.getFullName());
    }

    @Transactional(readOnly = true)
    public Customer findEntityByIdForCurrentCompany(Long id) {
        return customerRepository.findByIdAndCompanyId(id, currentUserService.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado con id: " + id));
    }

    private CustomerResponseDto toResponse(Customer customer) {
        return CustomerResponseDto.builder()
                .id(customer.getId())
                .companyId(customer.getCompany().getId())
                .fullName(customer.getFullName())
                .documentNumber(customer.getDocumentNumber())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .active(customer.getActive())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
