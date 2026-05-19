package com.stockalert.sales.service;

import com.stockalert.companies.model.Company;
import com.stockalert.companies.service.CompanyService;
import com.stockalert.customers.model.Customer;
import com.stockalert.customers.service.CustomerService;
import com.stockalert.alerts.service.StockAlertService;
import com.stockalert.inventory.model.InventoryMovementType;
import com.stockalert.inventory.service.InventoryMovementService;
import com.stockalert.products.model.Product;
import com.stockalert.products.service.ProductService;
import com.stockalert.sales.dto.SaleCreateDto;
import com.stockalert.sales.dto.SaleDetailResponseDto;
import com.stockalert.sales.dto.SaleItemCreateDto;
import com.stockalert.sales.dto.SaleResponseDto;
import com.stockalert.sales.model.Sale;
import com.stockalert.sales.model.SaleDetail;
import com.stockalert.sales.model.SaleStatus;
import com.stockalert.sales.repository.SaleRepository;
import com.stockalert.security.CurrentUserService;
import com.stockalert.shared.exception.BusinessException;
import com.stockalert.shared.exception.NotFoundException;
import com.stockalert.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductService productService;
    private final StockAlertService stockAlertService;
    private final CompanyService companyService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final CustomerService customerService;
    private final InventoryMovementService inventoryMovementService;

    @Transactional(readOnly = true)
    public List<SaleResponseDto> findAll() {
        return saleRepository.findByCompanyId(currentUserService.getCompanyId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<SaleResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection, SaleStatus status, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        Sort sort = buildSort(sortBy, sortDirection);
        PageRequest pageable = PageRequest.of(page, size, sort);
        Long companyId = currentUserService.getCompanyId();

        if (status != null && start != null && end != null) {
            return saleRepository.findByCompanyIdAndStatusAndSaleDateBetween(companyId, status, start, end, pageable).map(this::toResponse);
        }
        if (status != null && start != null) {
            return saleRepository.findByCompanyIdAndStatusAndSaleDateGreaterThanEqual(companyId, status, start, pageable).map(this::toResponse);
        }
        if (status != null && end != null) {
            return saleRepository.findByCompanyIdAndStatusAndSaleDateLessThanEqual(companyId, status, end, pageable).map(this::toResponse);
        }
        if (status != null) {
            return saleRepository.findByCompanyIdAndStatus(companyId, status, pageable).map(this::toResponse);
        }
        if (start != null && end != null) {
            return saleRepository.findByCompanyIdAndSaleDateBetween(companyId, start, end, pageable).map(this::toResponse);
        }
        if (start != null) {
            return saleRepository.findByCompanyIdAndSaleDateGreaterThanEqual(companyId, start, pageable).map(this::toResponse);
        }
        if (end != null) {
            return saleRepository.findByCompanyIdAndSaleDateLessThanEqual(companyId, end, pageable).map(this::toResponse);
        }
        return saleRepository.findByCompanyId(companyId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public SaleResponseDto findById(Long id) {
        Sale sale = saleRepository.findByIdAndCompanyId(id, currentUserService.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Venta no encontrada con id: " + id));
        return toResponse(sale);
    }

    @Transactional
    public SaleResponseDto create(SaleCreateDto request) {
        Company company = companyService.findEntityById(currentUserService.getCompanyId());
        Customer customer = request.getCustomerId() != null ? customerService.findEntityByIdForCurrentCompany(request.getCustomerId()) : null;
        Sale sale = Sale.builder()
                .company(company)
                .customer(customer)
                .saleNumber(generateSaleNumber(company.getId()))
                .total(BigDecimal.ZERO)
                .status(SaleStatus.ACTIVE)
                .createdBy(auditService.getCurrentUsername())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (SaleItemCreateDto item : request.getItems()) {
            Product product = productService.findEntityByIdForCurrentCompany(item.getProductId());
            validateProductForSale(product, item.getQuantity());

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            product.setStock(product.getStock() - item.getQuantity());
            stockAlertService.createIfNeeded(product);

            SaleDetail detail = SaleDetail.builder()
                    .company(company)
                    .product(product)
                    .createdBy(auditService.getCurrentUsername())
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();
            sale.addDetail(detail);

            total = total.add(subtotal);
        }

        sale.setTotal(total);
        Sale saved = saleRepository.save(sale);
        for (SaleDetail detail : saved.getDetails()) {
            Product product = detail.getProduct();
            inventoryMovementService.record(product, InventoryMovementType.SALE, detail.getQuantity(),
                    product.getStock() + detail.getQuantity(), product.getStock(), "SALE", saved.getId(), "Venta registrada");
        }
        return toResponse(saved);
    }

    @Transactional
    public SaleResponseDto cancel(Long id) {
        Sale sale = saleRepository.findByIdAndCompanyId(id, currentUserService.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Venta no encontrada con id: " + id));
        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new BusinessException("La venta ya se encuentra anulada");
        }

        for (SaleDetail detail : sale.getDetails()) {
            Product product = detail.getProduct();
            int previousStock = product.getStock();
            product.setStock(product.getStock() + detail.getQuantity());
            product.setUpdatedBy(auditService.getCurrentUsername());
            inventoryMovementService.record(product, InventoryMovementType.SALE_CANCEL, detail.getQuantity(),
                    previousStock, product.getStock(), "SALE", sale.getId(), "Anulacion de venta");
        }

        sale.setStatus(SaleStatus.CANCELLED);
        sale.setCancelledAt(java.time.LocalDateTime.now());
        sale.setCancelledBy(auditService.getCurrentUsername());
        return toResponse(sale);
    }

    private void validateProductForSale(Product product, Integer quantity) {
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BusinessException("El producto esta inactivo: " + product.getId());
        }
        if (product.getStock() < quantity) {
            throw new BusinessException("Stock insuficiente para el producto: " + product.getName());
        }
    }

    private SaleResponseDto toResponse(Sale sale) {
        List<SaleDetailResponseDto> details = sale.getDetails().stream()
                .map(detail -> SaleDetailResponseDto.builder()
                        .productId(detail.getProduct().getId())
                        .productName(detail.getProduct().getName())
                        .quantity(detail.getQuantity())
                        .unitPrice(detail.getUnitPrice())
                        .subtotal(detail.getSubtotal())
                        .build())
                .toList();

        return SaleResponseDto.builder()
                .id(sale.getId())
                .companyId(sale.getCompany().getId())
                .customerId(sale.getCustomer() != null ? sale.getCustomer().getId() : null)
                .customerName(sale.getCustomer() != null ? sale.getCustomer().getFullName() : null)
                .saleNumber(sale.getSaleNumber())
                .saleDate(sale.getSaleDate())
                .status(sale.getStatus())
                .cancelledAt(sale.getCancelledAt())
                .cancelledBy(sale.getCancelledBy())
                .total(sale.getTotal())
                .details(details)
                .build();
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }

    private String generateSaleNumber(Long companyId) {
        long next = saleRepository.countByCompanyId(companyId) + 1;
        return "V-" + String.format("%06d", next);
    }
}
