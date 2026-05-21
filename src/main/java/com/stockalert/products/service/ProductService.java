package com.stockalert.products.service;

import com.stockalert.companies.model.Company;
import com.stockalert.companies.service.CompanyService;
import com.stockalert.audit.service.AuditLogService;
import com.stockalert.inventory.model.InventoryMovement;
import com.stockalert.inventory.model.InventoryMovementType;
import com.stockalert.inventory.repository.InventoryMovementRepository;
import com.stockalert.products.dto.ProductCreateDto;
import com.stockalert.products.dto.ProductResponseDto;
import com.stockalert.products.dto.ProductUpdateDto;
import com.stockalert.products.model.Product;
import com.stockalert.products.repository.ProductRepository;
import com.stockalert.security.CurrentUserService;
import com.stockalert.shared.exception.NotFoundException;
import com.stockalert.shared.service.AuditService;
import com.stockalert.suppliers.model.Supplier;
import com.stockalert.suppliers.service.SupplierService;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyService companyService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final SupplierService supplierService;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<ProductResponseDto> findAll() {
        return productRepository.findByCompanyId(currentUserService.getCompanyId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection, String search, Boolean active) {
        Sort sort = buildSort(sortBy, sortDirection);
        PageRequest pageable = PageRequest.of(page, size, sort);
        Long companyId = currentUserService.getCompanyId();
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();

        if (normalizedSearch != null && active != null) {
            return productRepository.findByCompanyIdAndNameContainingIgnoreCaseAndActive(companyId, normalizedSearch, active, pageable)
                    .map(this::toResponse);
        }
        if (normalizedSearch != null) {
            return productRepository.findByCompanyIdAndNameContainingIgnoreCase(companyId, normalizedSearch, pageable)
                    .map(this::toResponse);
        }
        if (active != null) {
            return productRepository.findByCompanyIdAndActive(companyId, active, pageable)
                    .map(this::toResponse);
        }
        return productRepository.findByCompanyId(companyId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto findById(Long id) {
        return toResponse(findEntityByIdForCurrentCompany(id));
    }

    @Transactional
    public ProductResponseDto create(ProductCreateDto request) {
        Company company = companyService.findEntityById(currentUserService.getCompanyId());
        Supplier supplier = request.getSupplierId() != null ? supplierService.findEntityByIdForCurrentCompany(request.getSupplierId()) : null;
        Product product = Product.builder()
                .company(company)
                .supplier(supplier)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .cost(resolveCost(request.getCost()))
                .lastCost(resolveCost(request.getCost()))
                .stock(request.getStock())
                .minimumStock(request.getMinimumStock())
                .active(true)
                .createdBy(auditService.getCurrentUsername())
                .build();

        Product saved = productRepository.save(product);
        if (saved.getStock() > 0) {
            recordInventoryMovement(saved, InventoryMovementType.INITIAL_STOCK, saved.getStock(), 0, saved.getStock(),
                    "PRODUCT", saved.getId(), "Stock inicial del producto");
        }
        auditLogService.record("CREATE", "Product", saved.getId(), "Producto creado: " + saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public ProductResponseDto update(Long id, ProductUpdateDto request) {
        Product product = findEntityByIdForCurrentCompany(id);
        int previousStock = product.getStock();
        Supplier supplier = request.getSupplierId() != null ? supplierService.findEntityByIdForCurrentCompany(request.getSupplierId()) : null;
        product.setSupplier(supplier);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        if (request.getCost() != null) {
            product.setCost(request.getCost());
        }
        product.setStock(request.getStock());
        product.setMinimumStock(request.getMinimumStock());
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        product.setUpdatedBy(auditService.getCurrentUsername());
        registerStockUpdateMovement(product, previousStock, request.getStock());
        auditLogService.record("UPDATE", "Product", product.getId(), "Producto actualizado: " + product.getName());
        return toResponse(product);
    }

    @Transactional
    public void deactivate(Long id) {
        Product product = findEntityByIdForCurrentCompany(id);
        product.setActive(false);
        product.setUpdatedBy(auditService.getCurrentUsername());
        auditLogService.record("DEACTIVATE", "Product", product.getId(), "Producto desactivado: " + product.getName());
    }

    @Transactional
    public ProductResponseDto activate(Long id) {
        Product product = findEntityByIdForCurrentCompany(id);
        product.setActive(true);
        product.setDeletedAt(null);
        product.setUpdatedBy(auditService.getCurrentUsername());
        auditLogService.record("ACTIVATE", "Product", product.getId(), "Producto activado: " + product.getName());
        return toResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findEntityByIdForCurrentCompany(id);
        product.setActive(false);
        product.setDeletedAt(java.time.LocalDateTime.now());
        product.setUpdatedBy(auditService.getCurrentUsername());
        auditLogService.record("DELETE", "Product", product.getId(), "Producto eliminado: " + product.getName());
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> findLowStock() {
        return productRepository.findLowStockProductsByCompanyId(currentUserService.getCompanyId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Product findEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public Product findEntityByIdForCurrentCompany(Long id) {
        return productRepository.findByIdAndCompanyId(id, currentUserService.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con id: " + id));
    }

    private ProductResponseDto toResponse(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .companyId(product.getCompany().getId())
                .supplierId(product.getSupplier() != null ? product.getSupplier().getId() : null)
                .supplierName(product.getSupplier() != null ? product.getSupplier().getName() : null)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .cost(product.getCost())
                .lastCost(product.getLastCost())
                .stock(product.getStock())
                .minimumStock(product.getMinimumStock())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }

    private BigDecimal resolveCost(BigDecimal cost) {
        return cost != null ? cost : BigDecimal.ZERO;
    }

    private void registerStockUpdateMovement(Product product, int previousStock, int newStock) {
        if (newStock == previousStock) {
            return;
        }
        InventoryMovementType type = newStock > previousStock
                ? InventoryMovementType.ADJUSTMENT_IN
                : InventoryMovementType.ADJUSTMENT_OUT;
        int quantity = Math.abs(newStock - previousStock);
        recordInventoryMovement(product, type, quantity, previousStock, newStock, "PRODUCT", product.getId(), "Ajuste manual desde producto");
    }

    private void recordInventoryMovement(
            Product product,
            InventoryMovementType type,
            Integer quantity,
            Integer previousStock,
            Integer newStock,
            String referenceType,
            Long referenceId,
            String notes
    ) {
        inventoryMovementRepository.save(InventoryMovement.builder()
                .company(product.getCompany())
                .product(product)
                .type(type)
                .quantity(quantity)
                .previousStock(previousStock)
                .newStock(newStock)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .notes(notes)
                .createdBy(auditService.getCurrentUsername())
                .build());
    }
}
