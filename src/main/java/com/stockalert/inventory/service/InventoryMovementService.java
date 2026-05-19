package com.stockalert.inventory.service;

import com.stockalert.inventory.dto.InventoryAdjustmentDto;
import com.stockalert.inventory.dto.InventoryMovementResponseDto;
import com.stockalert.inventory.model.InventoryMovement;
import com.stockalert.inventory.model.InventoryMovementType;
import com.stockalert.inventory.repository.InventoryMovementRepository;
import com.stockalert.products.model.Product;
import com.stockalert.products.service.ProductService;
import com.stockalert.security.CurrentUserService;
import com.stockalert.shared.exception.BusinessException;
import com.stockalert.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryMovementService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductService productService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<InventoryMovementResponseDto> findAllPaginated(Long productId, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        if (productId != null) {
            return inventoryMovementRepository.findByCompanyIdAndProductId(currentUserService.getCompanyId(), productId, pageable)
                    .map(this::toResponse);
        }
        return inventoryMovementRepository.findByCompanyId(currentUserService.getCompanyId(), pageable).map(this::toResponse);
    }

    @Transactional
    public InventoryMovementResponseDto adjustmentIn(InventoryAdjustmentDto request) {
        Product product = productService.findEntityByIdForCurrentCompany(request.getProductId());
        int previousStock = product.getStock();
        product.setStock(previousStock + request.getQuantity());
        product.setUpdatedBy(auditService.getCurrentUsername());
        return toResponse(record(product, InventoryMovementType.ADJUSTMENT_IN, request.getQuantity(), previousStock, product.getStock(),
                "ADJUSTMENT", null, request.getNotes()));
    }

    @Transactional
    public InventoryMovementResponseDto adjustmentOut(InventoryAdjustmentDto request) {
        Product product = productService.findEntityByIdForCurrentCompany(request.getProductId());
        if (product.getStock() < request.getQuantity()) {
            throw new BusinessException("Stock insuficiente para realizar el ajuste");
        }
        int previousStock = product.getStock();
        product.setStock(previousStock - request.getQuantity());
        product.setUpdatedBy(auditService.getCurrentUsername());
        return toResponse(record(product, InventoryMovementType.ADJUSTMENT_OUT, request.getQuantity(), previousStock, product.getStock(),
                "ADJUSTMENT", null, request.getNotes()));
    }

    @Transactional
    public InventoryMovement record(
            Product product,
            InventoryMovementType type,
            Integer quantity,
            Integer previousStock,
            Integer newStock,
            String referenceType,
            Long referenceId,
            String notes
    ) {
        return inventoryMovementRepository.save(InventoryMovement.builder()
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

    private InventoryMovementResponseDto toResponse(InventoryMovement movement) {
        return InventoryMovementResponseDto.builder()
                .id(movement.getId())
                .companyId(movement.getCompany().getId())
                .productId(movement.getProduct().getId())
                .productName(movement.getProduct().getName())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .previousStock(movement.getPreviousStock())
                .newStock(movement.getNewStock())
                .referenceType(movement.getReferenceType())
                .referenceId(movement.getReferenceId())
                .notes(movement.getNotes())
                .createdAt(movement.getCreatedAt())
                .createdBy(movement.getCreatedBy())
                .build();
    }
}
