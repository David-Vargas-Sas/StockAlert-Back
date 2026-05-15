package com.stockalert.sales.service;

import com.stockalert.companies.model.Company;
import com.stockalert.companies.service.CompanyService;
import com.stockalert.alerts.service.StockAlertService;
import com.stockalert.products.model.Product;
import com.stockalert.products.service.ProductService;
import com.stockalert.sales.dto.SaleCreateDto;
import com.stockalert.sales.dto.SaleDetailResponseDto;
import com.stockalert.sales.dto.SaleItemCreateDto;
import com.stockalert.sales.dto.SaleResponseDto;
import com.stockalert.sales.model.Sale;
import com.stockalert.sales.model.SaleDetail;
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

    @Transactional(readOnly = true)
    public List<SaleResponseDto> findAll() {
        return saleRepository.findByCompanyId(currentUserService.getCompanyId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<SaleResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort sort = buildSort(sortBy, sortDirection);
        return saleRepository.findByCompanyId(currentUserService.getCompanyId(), PageRequest.of(page, size, sort))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public SaleResponseDto findById(Long id) {
        Sale sale = saleRepository.findByIdAndCompanyId(id, currentUserService.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + id));
        return toResponse(sale);
    }

    @Transactional
    public SaleResponseDto create(SaleCreateDto request) {
        Company company = companyService.findEntityById(currentUserService.getCompanyId());
        Sale sale = Sale.builder()
                .company(company)
                .total(BigDecimal.ZERO)
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
        return toResponse(saleRepository.save(sale));
    }

    private void validateProductForSale(Product product, Integer quantity) {
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BusinessException("Product is inactive: " + product.getId());
        }
        if (product.getStock() < quantity) {
            throw new BusinessException("Insufficient stock for product: " + product.getName());
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
                .saleDate(sale.getSaleDate())
                .total(sale.getTotal())
                .details(details)
                .build();
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }
}
