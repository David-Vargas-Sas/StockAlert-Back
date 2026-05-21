package com.stockalert.purchases.service;

import com.stockalert.companies.model.Company;
import com.stockalert.companies.service.CompanyService;
import com.stockalert.audit.service.AuditLogService;
import com.stockalert.inventory.model.InventoryMovementType;
import com.stockalert.inventory.service.InventoryMovementService;
import com.stockalert.products.model.Product;
import com.stockalert.products.service.ProductService;
import com.stockalert.purchases.dto.PurchaseCreateDto;
import com.stockalert.purchases.dto.PurchaseDetailResponseDto;
import com.stockalert.purchases.dto.PurchaseItemCreateDto;
import com.stockalert.purchases.dto.PurchaseResponseDto;
import com.stockalert.purchases.model.Purchase;
import com.stockalert.purchases.model.PurchaseDetail;
import com.stockalert.purchases.model.PurchaseStatus;
import com.stockalert.purchases.repository.PurchaseRepository;
import com.stockalert.security.CurrentUserService;
import com.stockalert.shared.exception.BusinessException;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final CompanyService companyService;
    private final SupplierService supplierService;
    private final ProductService productService;
    private final InventoryMovementService inventoryMovementService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<PurchaseResponseDto> findAll() {
        return purchaseRepository.findByCompanyId(currentUserService.getCompanyId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<PurchaseResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return purchaseRepository.findByCompanyId(currentUserService.getCompanyId(), PageRequest.of(page, size, Sort.by(direction, sortBy)))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PurchaseResponseDto findById(Long id) {
        return toResponse(findEntityByIdForCurrentCompany(id));
    }

    @Transactional
    public PurchaseResponseDto create(PurchaseCreateDto request) {
        Company company = companyService.findEntityById(currentUserService.getCompanyId());
        Supplier supplier = request.getSupplierId() != null ? supplierService.findEntityByIdForCurrentCompany(request.getSupplierId()) : null;
        Purchase purchase = Purchase.builder()
                .company(company)
                .supplier(supplier)
                .status(PurchaseStatus.RECEIVED)
                .total(BigDecimal.ZERO)
                .createdBy(auditService.getCurrentUsername())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseItemCreateDto item : request.getItems()) {
            Product product = productService.findEntityByIdForCurrentCompany(item.getProductId());
            BigDecimal subtotal = item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity()));
            int previousStock = product.getStock();
            BigDecimal previousCost = product.getCost();
            product.setStock(previousStock + item.getQuantity());
            updateWeightedAverageCost(product, previousStock, previousCost, item.getQuantity(), item.getUnitCost());
            product.setUpdatedBy(auditService.getCurrentUsername());

            PurchaseDetail detail = PurchaseDetail.builder()
                    .company(company)
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitCost(item.getUnitCost())
                    .subtotal(subtotal)
                    .build();
            purchase.addDetail(detail);
            total = total.add(subtotal);
        }

        purchase.setTotal(total);
        Purchase saved = purchaseRepository.save(purchase);
        for (PurchaseDetail detail : saved.getDetails()) {
            Product product = detail.getProduct();
            inventoryMovementService.record(product, InventoryMovementType.PURCHASE, detail.getQuantity(),
                    product.getStock() - detail.getQuantity(), product.getStock(), "PURCHASE", saved.getId(), "Compra recibida");
        }
        auditLogService.record("CREATE", "Purchase", saved.getId(), "Compra registrada");
        return toResponse(saved);
    }

    @Transactional
    public PurchaseResponseDto cancel(Long id) {
        Purchase purchase = findEntityByIdForCurrentCompany(id);
        if (purchase.getStatus() == PurchaseStatus.CANCELLED) {
            throw new BusinessException("La compra ya se encuentra anulada");
        }
        for (PurchaseDetail detail : purchase.getDetails()) {
            Product product = detail.getProduct();
            if (product.getStock() < detail.getQuantity()) {
                throw new BusinessException("No se puede anular la compra porque el producto no tiene stock suficiente: " + product.getName());
            }
        }
        for (PurchaseDetail detail : purchase.getDetails()) {
            Product product = detail.getProduct();
            int previousStock = product.getStock();
            BigDecimal previousCost = product.getCost();
            product.setStock(previousStock - detail.getQuantity());
            rollbackWeightedAverageCost(product, previousStock, previousCost, detail.getQuantity(), detail.getUnitCost());
            product.setUpdatedBy(auditService.getCurrentUsername());
            inventoryMovementService.record(product, InventoryMovementType.PURCHASE_CANCEL, detail.getQuantity(),
                    previousStock, product.getStock(), "PURCHASE", purchase.getId(), "Anulacion de compra");
        }
        purchase.setStatus(PurchaseStatus.CANCELLED);
        purchase.setCancelledAt(LocalDateTime.now());
        purchase.setCancelledBy(auditService.getCurrentUsername());
        auditLogService.record("CANCEL", "Purchase", purchase.getId(), "Compra anulada");
        return toResponse(purchase);
    }

    private Purchase findEntityByIdForCurrentCompany(Long id) {
        return purchaseRepository.findByIdAndCompanyId(id, currentUserService.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Compra no encontrada con id: " + id));
    }

    private PurchaseResponseDto toResponse(Purchase purchase) {
        List<PurchaseDetailResponseDto> details = purchase.getDetails().stream()
                .map(detail -> PurchaseDetailResponseDto.builder()
                        .productId(detail.getProduct().getId())
                        .productName(detail.getProduct().getName())
                        .quantity(detail.getQuantity())
                        .unitCost(detail.getUnitCost())
                        .subtotal(detail.getSubtotal())
                        .build())
                .toList();

        return PurchaseResponseDto.builder()
                .id(purchase.getId())
                .companyId(purchase.getCompany().getId())
                .supplierId(purchase.getSupplier() != null ? purchase.getSupplier().getId() : null)
                .supplierName(purchase.getSupplier() != null ? purchase.getSupplier().getName() : null)
                .purchaseDate(purchase.getPurchaseDate())
                .status(purchase.getStatus())
                .statusLabel(purchase.getStatus().getLabel())
                .total(purchase.getTotal())
                .cancelledAt(purchase.getCancelledAt())
                .cancelledBy(purchase.getCancelledBy())
                .details(details)
                .build();
    }

    private void updateWeightedAverageCost(Product product, int previousStock, BigDecimal previousCost, int purchasedQuantity, BigDecimal unitCost) {
        int newStock = previousStock + purchasedQuantity;
        BigDecimal currentInventoryValue = safeCost(previousCost).multiply(BigDecimal.valueOf(previousStock));
        BigDecimal purchaseValue = unitCost.multiply(BigDecimal.valueOf(purchasedQuantity));
        BigDecimal newAverage = newStock > 0
                ? currentInventoryValue.add(purchaseValue).divide(BigDecimal.valueOf(newStock), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        product.setCost(newAverage);
        product.setLastCost(unitCost);
    }

    private void rollbackWeightedAverageCost(Product product, int previousStock, BigDecimal previousCost, int cancelledQuantity, BigDecimal unitCost) {
        int newStock = previousStock - cancelledQuantity;
        if (newStock <= 0) {
            product.setCost(BigDecimal.ZERO);
            return;
        }
        BigDecimal previousInventoryValue = safeCost(previousCost).multiply(BigDecimal.valueOf(previousStock));
        BigDecimal cancelledValue = unitCost.multiply(BigDecimal.valueOf(cancelledQuantity));
        BigDecimal adjustedValue = previousInventoryValue.subtract(cancelledValue);
        if (adjustedValue.compareTo(BigDecimal.ZERO) < 0) {
            adjustedValue = BigDecimal.ZERO;
        }
        product.setCost(adjustedValue.divide(BigDecimal.valueOf(newStock), 2, RoundingMode.HALF_UP));
    }

    private BigDecimal safeCost(BigDecimal cost) {
        return cost != null ? cost : BigDecimal.ZERO;
    }
}
