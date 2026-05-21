package com.stockalert.sales.service;

import com.stockalert.sales.model.Sale;
import com.stockalert.sales.model.SaleDetail;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    private static final float MARGIN = 48;
    private static final PDType1Font FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final Color INK = new Color(15, 23, 42);
    private static final Color MUTED = new Color(71, 85, 105);
    private static final Color TEAL = new Color(0, 124, 122);
    private static final Color NAVY = new Color(17, 24, 39);
    private static final Color PALE_BLUE = new Color(243, 246, 252);
    private static final Color BORDER = new Color(217, 222, 232);

    public byte[] generate(Sale sale) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                drawBackground(content, page);

                float left = 60;
                float right = 552;
                float top = 735;

                drawBadge(content, left, top - 10);
                drawText(content, sale.getCompany().getName(), left, top - 58, FONT_BOLD, 11, INK);
                drawCompanyLine(content, sale.getCompany().getAddress(), left, top - 73);
                drawCompanyLine(content, contactLine(sale), left, top - 87);

                drawTextRight(content, "FACTURA", right, top - 5, FONT_BOLD, 24, TEAL);
                drawFilledRect(content, 456, top - 42, 96, 24, new Color(238, 243, 255));
                drawText(content, "Nro. " + invoiceNumber(sale), 468, top - 34, FONT_BOLD, 9, INK);

                float metaTop = 575;
                drawText(content, "FACTURAR A", left, metaTop, FONT_BOLD, 8, new Color(31, 55, 99));
                drawText(content, sale.getCustomer().getFullName(), left, metaTop - 18, FONT_BOLD, 10, INK);
                drawOptionalText(content, "Documento: " + sale.getCustomer().getDocumentNumber(), left, metaTop - 34);
                drawOptionalText(content, sale.getCustomer().getAddress(), left, metaTop - 48);
                drawOptionalText(content, sale.getCustomer().getEmail(), left, metaTop - 62);

                drawFilledRect(content, 318, metaTop - 82, 234, 98, PALE_BLUE);
                drawText(content, "EMISION", 333, metaTop - 18, FONT_BOLD, 7, new Color(31, 55, 99));
                drawText(content, formatDate(sale), 333, metaTop - 34, FONT_BOLD, 9, INK);
                drawText(content, "ESTADO", 444, metaTop - 18, FONT_BOLD, 7, new Color(31, 55, 99));
                drawText(content, sale.getStatus().getLabel(), 444, metaTop - 34, FONT_BOLD, 9, INK);
                drawText(content, "VENDEDOR", 333, metaTop - 62, FONT_BOLD, 7, new Color(31, 55, 99));
                drawText(content, safe(sale.getCreatedBy()), 333, metaTop - 78, FONT_BOLD, 9, INK);

                float tableTop = 435;
                drawText(content, "DESCRIPCION", left, tableTop, FONT_BOLD, 8, new Color(31, 55, 99));
                drawText(content, "SKU", 284, tableTop, FONT_BOLD, 8, new Color(31, 55, 99));
                drawText(content, "CANT.", 365, tableTop, FONT_BOLD, 8, new Color(31, 55, 99));
                drawTextRight(content, "UNITARIO", 484, tableTop, FONT_BOLD, 8, new Color(31, 55, 99));
                drawTextRight(content, "SUBTOTAL", right, tableTop, FONT_BOLD, 8, new Color(31, 55, 99));
                drawLine(content, left, tableTop - 10, right, tableTop - 10, NAVY, 1.1f);

                float y = tableTop - 34;
                for (SaleDetail detail : sale.getDetails()) {
                    drawText(content, truncate(detail.getProduct().getName(), 34), left, y, FONT_BOLD, 9, INK);
                    drawText(content, truncate(detail.getProduct().getDescription(), 36), left, y - 13, FONT, 8, MUTED);
                    drawText(content, "PROD-" + detail.getProduct().getId(), 284, y, FONT, 8, MUTED);
                    drawText(content, String.valueOf(detail.getQuantity()), 374, y, FONT, 8, INK);
                    drawTextRight(content, formatMoney(detail.getUnitPrice()), 484, y, FONT, 8, INK);
                    drawTextRight(content, formatMoney(detail.getSubtotal()), right, y, FONT_BOLD, 8, INK);
                    drawLine(content, left, y - 28, right, y - 28, new Color(229, 231, 235), 0.6f);
                    y -= 45;
                }

                float totalsTop = Math.max(y - 24, 180);
                drawText(content, "TERMINOS Y CONDICIONES", left, totalsTop, FONT_BOLD, 8, new Color(31, 55, 99));
                drawText(content, "Este documento corresponde al resumen de la venta registrada", left, totalsTop - 18, FONT, 8, INK);
                drawText(content, "en StockAlert. Conserva este correo para futuras consultas.", left, totalsTop - 31, FONT, 8, INK);
                drawText(content, "Gracias por confiar en " + sale.getCompany().getName() + ".", left, totalsTop - 55, FONT_BOLD, 8, TEAL);

                drawText(content, "Neto", 370, totalsTop - 3, FONT, 9, INK);
                drawTextRight(content, formatMoney(sale.getTotal()), right, totalsTop - 3, FONT, 9, INK);
                drawLine(content, 370, totalsTop - 21, right, totalsTop - 21, TEAL, 1.2f);
                drawText(content, "TOTAL", 370, totalsTop - 42, FONT_BOLD, 13, TEAL);
                drawTextRight(content, formatMoney(sale.getTotal()), right, totalsTop - 42, FONT_BOLD, 13, TEAL);

                drawLine(content, left, 85, right, 85, BORDER, 0.8f);
                drawText(content, "Validacion digital StockAlert | Factura electronica", left, 62, FONT, 8, MUTED);
            }

            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo generar el PDF de la factura", exception);
        }
    }

    private void drawText(PDPageContentStream content, String text, float x, float y, PDType1Font font, int size) throws IOException {
        drawText(content, text, x, y, font, size, INK);
    }

    private void drawText(PDPageContentStream content, String text, float x, float y, PDType1Font font, int size, Color color) throws IOException {
        content.setNonStrokingColor(color);
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(safe(text));
        content.endText();
    }

    private void drawTextRight(PDPageContentStream content, String text, float rightX, float y, PDType1Font font, int size) throws IOException {
        drawTextRight(content, text, rightX, y, font, size, INK);
    }

    private void drawTextRight(PDPageContentStream content, String text, float rightX, float y, PDType1Font font, int size, Color color) throws IOException {
        float width = font.getStringWidth(safe(text)) / 1000 * size;
        drawText(content, text, rightX - width, y, font, size, color);
    }

    private void drawLine(PDPageContentStream content, float x1, float y1, float x2, float y2) throws IOException {
        drawLine(content, x1, y1, x2, y2, INK, 1f);
    }

    private void drawLine(PDPageContentStream content, float x1, float y1, float x2, float y2, Color color, float width) throws IOException {
        content.setStrokingColor(color);
        content.setLineWidth(width);
        content.moveTo(x1, y1);
        content.lineTo(x2, y2);
        content.stroke();
    }

    private void drawBackground(PDPageContentStream content, PDPage page) throws IOException {
        drawFilledRect(content, 0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight(), new Color(238, 241, 245));
        drawFilledRect(content, 28, 18, page.getMediaBox().getWidth() - 56, page.getMediaBox().getHeight() - 36, Color.WHITE);
        content.setStrokingColor(BORDER);
        content.setLineWidth(0.8f);
        content.addRect(28, 18, page.getMediaBox().getWidth() - 56, page.getMediaBox().getHeight() - 36);
        content.stroke();
    }

    private void drawBadge(PDPageContentStream content, float x, float y) throws IOException {
        drawFilledRect(content, x, y - 36, 36, 36, NAVY);
        drawText(content, "S", x + 13, y - 23, FONT_BOLD, 12, Color.WHITE);
    }

    private void drawFilledRect(PDPageContentStream content, float x, float y, float width, float height, Color color) throws IOException {
        content.setNonStrokingColor(color);
        content.addRect(x, y, width, height);
        content.fill();
    }

    private void drawCompanyLine(PDPageContentStream content, String value, float x, float y) throws IOException {
        if (value != null && !value.isBlank()) {
            drawText(content, value, x, y, FONT, 8, INK);
        }
    }

    private void drawOptionalText(PDPageContentStream content, String value, float x, float y) throws IOException {
        if (value != null && !value.isBlank() && !value.endsWith("null")) {
            drawText(content, value, x, y, FONT, 8, INK);
        }
    }

    private String contactLine(Sale sale) {
        String city = sale.getCompany().getCity();
        String email = sale.getCompany().getEmail();
        if (city != null && !city.isBlank() && email != null && !email.isBlank()) {
            return city + " | " + email;
        }
        if (email != null && !email.isBlank()) {
            return email;
        }
        return "";
    }

    private String invoiceNumber(Sale sale) {
        return sale.getInvoiceNumber() != null ? sale.getInvoiceNumber() : sale.getSaleNumber();
    }

    private String formatDate(Sale sale) {
        return sale.getSaleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private String formatMoney(BigDecimal value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        return format.format(value);
    }

    private String truncate(String value, int max) {
        String safe = safe(value);
        return safe.length() <= max ? safe : safe.substring(0, max - 3) + "...";
    }

    private String safe(String value) {
        return value == null ? "" : value
                .replace("\n", " ")
                .replace("\r", " ")
                .replace('\u00A0', ' ');
    }
}
