package com.stockalert.alerts.service;

import com.stockalert.alerts.dto.StockAlertResponseDto;
import com.stockalert.alerts.model.AlertStatus;
import com.stockalert.alerts.model.StockAlert;
import com.stockalert.alerts.repository.StockAlertRepository;
import com.stockalert.products.model.Product;
import com.stockalert.security.CurrentUserService;
import com.stockalert.shared.exception.NotFoundException;
import com.stockalert.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockAlertService {

    private final StockAlertRepository stockAlertRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<StockAlertResponseDto> findAll() {
        return stockAlertRepository.findByCompanyId(currentUserService.getCompanyId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<StockAlertResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort sort = buildSort(sortBy, sortDirection);
        return stockAlertRepository.findByCompanyId(currentUserService.getCompanyId(), PageRequest.of(page, size, sort))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<StockAlertResponseDto> findActive() {
        return stockAlertRepository.findByCompanyIdAndStatus(currentUserService.getCompanyId(), AlertStatus.ACTIVE).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StockAlertResponseDto resolve(Long id) {
        StockAlert alert = stockAlertRepository.findByIdAndCompanyId(id, currentUserService.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Alerta de stock no encontrada con id: " + id));
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setUpdatedBy(auditService.getCurrentUsername());
        return toResponse(alert);
    }

    public void createIfNeeded(Product product) {
        if (product.getStock() > product.getMinimumStock()) {
            return;
        }
        boolean activeAlertExists = stockAlertRepository.existsByProductIdAndCompanyIdAndStatus(
                product.getId(),
                product.getCompany().getId(),
                AlertStatus.ACTIVE
        );
        if (activeAlertExists) {
            return;
        }

        StockAlert alert = StockAlert.builder()
                .product(product)
                .company(product.getCompany())
                .status(AlertStatus.ACTIVE)
                .createdBy(auditService.getCurrentUsername())
                .message("El producto " + product.getName() + " necesita reposicion. Stock actual: " + product.getStock())
                .build();
        stockAlertRepository.save(alert);
    }

    private StockAlertResponseDto toResponse(StockAlert alert) {
        Product product = alert.getProduct();
        return StockAlertResponseDto.builder()
                .id(alert.getId())
                .companyId(alert.getCompany().getId())
                .productId(product.getId())
                .productName(product.getName())
                .message(alert.getMessage())
                .status(alert.getStatus())
                .statusLabel(alert.getStatus().getLabel())
                .createdAt(alert.getCreatedAt())
                .build();
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }
}
