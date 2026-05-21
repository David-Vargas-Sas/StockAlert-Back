package com.stockalert.sales.service;

import com.stockalert.companies.model.Company;
import com.stockalert.companies.service.CompanyService;
import com.stockalert.audit.service.AuditLogService;
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
import com.stockalert.shared.email.EmailService;
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
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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
    private final AuditLogService auditLogService;
    private final EmailService emailService;
    private final InvoicePdfService invoicePdfService;

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
    public List<SaleResponseDto> findByCustomer(Long customerId) {
        customerService.findEntityByIdForCurrentCompany(customerId);
        return saleRepository.findByCompanyIdAndCustomerIdOrderBySaleDateDesc(currentUserService.getCompanyId(), customerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<SaleResponseDto> findByCustomerPaginated(Long customerId, int page, int size, String sortBy, String sortDirection, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        customerService.findEntityByIdForCurrentCompany(customerId);
        Sort sort = buildSort(sortBy, sortDirection);
        PageRequest pageable = PageRequest.of(page, size, sort);
        Long companyId = currentUserService.getCompanyId();

        if (start != null && end != null) {
            return saleRepository.findByCompanyIdAndCustomerIdAndSaleDateBetween(companyId, customerId, start, end, pageable)
                    .map(this::toResponse);
        }
        if (start != null) {
            return saleRepository.findByCompanyIdAndCustomerIdAndSaleDateGreaterThanEqual(companyId, customerId, start, pageable)
                    .map(this::toResponse);
        }
        if (end != null) {
            return saleRepository.findByCompanyIdAndCustomerIdAndSaleDateLessThanEqual(companyId, customerId, end, pageable)
                    .map(this::toResponse);
        }
        return saleRepository.findByCompanyIdAndCustomerId(companyId, customerId, pageable)
                .map(this::toResponse);
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
                .invoiceNumber(generateInvoiceNumber(company.getId()))
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
        auditLogService.record("CREATE", "Sale", saved.getId(), "Venta registrada: " + saved.getSaleNumber());
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
        auditLogService.record("CANCEL", "Sale", sale.getId(), "Venta anulada: " + sale.getSaleNumber());
        return toResponse(sale);
    }

    @Transactional
    public void sendInvoiceEmail(Long id) {
        Sale sale = saleRepository.findByIdAndCompanyId(id, currentUserService.getCompanyId())
                .orElseThrow(() -> new NotFoundException("Venta no encontrada con id: " + id));
        if (sale.getCustomer() == null) {
            throw new BusinessException("La venta no tiene un cliente asociado");
        }
        if (sale.getCustomer().getEmail() == null || sale.getCustomer().getEmail().isBlank()) {
            throw new BusinessException("El cliente no tiene correo registrado");
        }

        ensureInvoiceNumber(sale);
        byte[] invoicePdf = invoicePdfService.generate(sale);

        emailService.sendHtmlWithAttachment(
                sale.getCustomer().getEmail(),
                "Factura de venta " + sale.getInvoiceNumber(),
                buildInvoiceEmailHtml(sale),
                invoicePdf,
                "factura-" + sale.getInvoiceNumber() + ".pdf"
        );
        auditLogService.record("SEND_INVOICE_EMAIL", "Sale", sale.getId(), "Factura enviada por correo: " + sale.getSaleNumber());
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
                .invoiceNumber(resolveInvoiceNumber(sale))
                .saleDate(sale.getSaleDate())
                .createdBy(resolveSellerName(sale))
                .sellerName(resolveSellerName(sale))
                .status(sale.getStatus())
                .statusLabel(sale.getStatus().getLabel())
                .cancelledAt(sale.getCancelledAt())
                .cancelledBy(sale.getCancelledBy())
                .total(sale.getTotal())
                .details(details)
                .build();
    }

    private String resolveSellerName(Sale sale) {
        return sale.getCreatedBy() != null && !sale.getCreatedBy().isBlank()
                ? sale.getCreatedBy()
                : "No registrado";
    }

    private String buildInvoiceEmailHtml(Sale sale) {
        StringBuilder rows = new StringBuilder();
        for (SaleDetail detail : sale.getDetails()) {
            rows.append("<tr>")
                    .append("<td class=\"desc\"><strong>").append(escapeHtml(detail.getProduct().getName())).append("</strong><br>")
                    .append("<span>").append(escapeHtml(defaultText(detail.getProduct().getDescription(), "Sin descripcion"))).append("</span></td>")
                    .append("<td class=\"sku\">PROD-").append(detail.getProduct().getId()).append("</td>")
                    .append("<td class=\"center\">").append(detail.getQuantity()).append("</td>")
                    .append("<td class=\"money\">").append(formatMoney(detail.getUnitPrice())).append("</td>")
                    .append("<td class=\"money\"><strong>").append(formatMoney(detail.getSubtotal())).append("</strong></td>")
                    .append("</tr>");
        }

        return """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <style>
                    body { margin: 0; padding: 0; background: #eef1f5; font-family: Arial, sans-serif; color: #0f172a; }
                    .page { max-width: 760px; margin: 0 auto; padding: 18px; }
                    .invoice { background: #ffffff; border: 1px solid #d9dee8; padding: 44px 40px; }
                    .top { width: 100%%; border-collapse: collapse; margin-bottom: 34px; }
                    .brand-cell { width: 60%%; vertical-align: top; }
                    .title-cell { width: 40%%; vertical-align: top; text-align: right; }
                    .logo { width: 46px; height: 46px; border-radius: 8px; background: #111827; color: #ffffff; line-height: 46px; text-align: center; font-size: 20px; font-weight: bold; margin-bottom: 16px; }
                    .company { font-size: 13px; line-height: 1.55; }
                    .company strong { font-size: 14px; }
                    h1 { margin: 0; color: #007c7a; font-size: 30px; letter-spacing: 1px; }
                    .folio { display: inline-block; margin-top: 10px; padding: 9px 13px; background: #eef3ff; color: #0f172a; font-weight: bold; font-size: 13px; }
                    .meta { width: 100%%; border-collapse: collapse; margin-bottom: 34px; }
                    .bill-to { width: 52%%; vertical-align: top; padding-right: 24px; }
                    .box { width: 48%%; background: #f3f6fc; padding: 20px; vertical-align: top; }
                    .label { font-size: 10px; color: #1f3763; text-transform: uppercase; font-weight: bold; letter-spacing: .5px; margin-bottom: 8px; }
                    .value { font-size: 13px; line-height: 1.55; margin-bottom: 10px; }
                    .info-grid { width: 100%%; border-collapse: collapse; }
                    .info-grid td { width: 50%%; padding: 0 8px 12px 0; vertical-align: top; border: 0; }
                    .items { width: 100%%; border-collapse: collapse; margin-top: 8px; }
                    .items th { padding: 12px 0; border-bottom: 2px solid #0f172a; color: #1f3763; font-size: 10px; text-transform: uppercase; text-align: left; letter-spacing: .5px; }
                    .items td { padding: 18px 0; border-bottom: 1px solid #e5e7eb; font-size: 13px; vertical-align: top; }
                    .desc { width: 45%%; }
                    .desc span { color: #64748b; font-size: 12px; }
                    .sku { width: 17%%; color: #475569; font-size: 12px; }
                    .center { text-align: center; }
                    .money { text-align: right; white-space: nowrap; }
                    .totals { width: 100%%; border-collapse: collapse; margin-top: 28px; }
                    .terms { width: 55%%; vertical-align: top; padding-top: 18px; }
                    .sum { width: 45%%; vertical-align: top; }
                    .sum table { width: 100%%; border-collapse: collapse; }
                    .sum td { padding: 7px 0; font-size: 13px; border: 0; }
                    .sum .grand td { border-top: 2px solid #00928f; color: #007c7a; font-weight: bold; font-size: 19px; padding-top: 13px; }
                    .footer { margin-top: 36px; padding-top: 22px; border-top: 1px solid #d9dee8; font-size: 12px; color: #475569; }
                    .thanks { color: #007c7a; font-weight: bold; margin-top: 12px; }
                  </style>
                </head>
                <body>
                  <div class="page">
                    <div class="invoice">
                      <table class="top">
                        <tr>
                          <td class="brand-cell">
                            <div class="logo">S</div>
                            <div class="company">
                              <strong>%s</strong><br>
                              %s
                            </div>
                          </td>
                          <td class="title-cell">
                            <h1>FACTURA</h1>
                            <div class="folio">Nro. %s</div>
                          </td>
                        </tr>
                      </table>
                      <table class="meta">
                        <tr>
                          <td class="bill-to">
                            <div class="label">Facturar a</div>
                            <div class="value">
                              <strong>%s</strong><br>
                              %s
                              %s
                            </div>
                          </td>
                          <td class="box">
                            <table class="info-grid">
                              <tr>
                                <td>
                                  <div class="label">Emision</div>
                                  <strong>%s</strong>
                                </td>
                                <td>
                                  <div class="label">Estado</div>
                                  <strong>%s</strong>
                                </td>
                              </tr>
                              <tr>
                                <td colspan="2">
                                  <div class="label">Vendedor</div>
                                  <strong>%s</strong>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                      </table>
                      <table class="items">
                        <thead>
                          <tr>
                            <th>Descripcion</th>
                            <th>SKU</th>
                            <th style="text-align:center;">Cant.</th>
                            <th style="text-align:right;">Unitario</th>
                            <th style="text-align:right;">Subtotal</th>
                          </tr>
                        </thead>
                        <tbody>
                          %s
                        </tbody>
                      </table>
                      <table class="totals">
                        <tr>
                          <td class="terms">
                            <div class="label">Terminos y condiciones</div>
                            <div class="value">
                              Este documento corresponde al resumen de la venta registrada en StockAlert.
                              Conserva este correo para futuras consultas o solicitudes de soporte.
                            </div>
                            <div class="thanks">Gracias por confiar en %s.</div>
                          </td>
                          <td class="sum">
                            <table>
                              <tr>
                                <td>Neto</td>
                                <td class="money">%s</td>
                              </tr>
                              <tr class="grand">
                                <td>Total</td>
                                <td class="money">%s</td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                      </table>
                      <div class="footer">
                        Validacion digital StockAlert | Factura electronica
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                escapeHtml(sale.getCompany().getName()),
                buildCompanyContactHtml(sale),
                escapeHtml(resolveInvoiceNumber(sale)),
                escapeHtml(sale.getCustomer().getFullName()),
                optionalLine("Documento: ", sale.getCustomer().getDocumentNumber()),
                buildCustomerContactHtml(sale),
                formatDateTime(sale),
                escapeHtml(sale.getStatus().getLabel()),
                escapeHtml(resolveSellerName(sale)),
                rows,
                escapeHtml(sale.getCompany().getName()),
                formatMoney(sale.getTotal()),
                formatMoney(sale.getTotal())
        );
    }

    private String defaultText(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private void ensureInvoiceNumber(Sale sale) {
        if (sale.getInvoiceNumber() == null || sale.getInvoiceNumber().isBlank()) {
            sale.setInvoiceNumber(generateInvoiceNumber(sale.getCompany().getId()));
        }
    }

    private String resolveInvoiceNumber(Sale sale) {
        return sale.getInvoiceNumber() != null && !sale.getInvoiceNumber().isBlank()
                ? sale.getInvoiceNumber()
                : sale.getSaleNumber();
    }

    private String buildCompanyContactHtml(Sale sale) {
        StringBuilder html = new StringBuilder();
        appendOptionalLine(html, sale.getCompany().getAddress());
        appendOptionalLine(html, sale.getCompany().getCity());
        appendOptionalLine(html, sale.getCompany().getEmail());
        appendOptionalLine(html, sale.getCompany().getPhone());
        return html.isEmpty() ? "Datos de contacto pendientes" : html.toString();
    }

    private String buildCustomerContactHtml(Sale sale) {
        StringBuilder html = new StringBuilder();
        appendOptionalLine(html, sale.getCustomer().getAddress());
        appendOptionalLine(html, sale.getCustomer().getEmail());
        appendOptionalLine(html, sale.getCustomer().getPhone());
        return html.toString();
    }

    private void appendOptionalLine(StringBuilder html, String value) {
        if (value != null && !value.isBlank()) {
            html.append(escapeHtml(value)).append("<br>");
        }
    }

    private String optionalLine(String label, String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return escapeHtml(label + value) + "<br>";
    }

    private String formatDateTime(Sale sale) {
        return sale.getSaleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private String formatMoney(BigDecimal value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        return format.format(value);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }

    private String generateSaleNumber(Long companyId) {
        long next = saleRepository.countByCompanyId(companyId) + 1;
        return "V-" + String.format("%06d", next);
    }

    private String generateInvoiceNumber(Long companyId) {
        long next = saleRepository.countByCompanyIdAndInvoiceNumberIsNotNull(companyId) + 1;
        return "FE-" + String.format("%06d", next);
    }
}
