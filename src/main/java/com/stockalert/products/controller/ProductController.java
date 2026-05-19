package com.stockalert.products.controller;

import com.stockalert.products.dto.ProductCreateDto;
import com.stockalert.products.dto.ProductResponseDto;
import com.stockalert.products.dto.ProductUpdateDto;
import com.stockalert.products.service.ProductService;
import com.stockalert.shared.response.ApiResponseDto;
import com.stockalert.shared.response.PageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Productos", description = "Gestion de productos por empresa")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "Obtener todos los productos de la empresa autenticada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<ApiResponseDto<List<ProductResponseDto>>> findAll() {
        logger.info("Solicitud para obtener productos");
        return ResponseEntity.ok(ApiResponseDto.success("Productos obtenidos correctamente", productService.findAll()));
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "Obtener productos paginados")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ProductResponseDto>>> findAllPaginated(
            @RequestParam(defaultValue = "0") @Parameter(description = "Numero de pagina") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Tamano de pagina") int size,
            @RequestParam(defaultValue = "id") @Parameter(description = "Campo por el que ordenar") String sortBy,
            @RequestParam(defaultValue = "asc") @Parameter(description = "Direccion de ordenamiento") String sortDirection,
            @RequestParam(required = false) @Parameter(description = "Busqueda por nombre") String search,
            @RequestParam(required = false) @Parameter(description = "Filtrar por estado activo") Boolean active) {
        logger.info("Solicitud paginada productos - page: {}, size: {}, sortBy: {}, direction: {}", page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success(
                "Productos paginados obtenidos correctamente",
                PageResponseDto.from(productService.findAllPaginated(page, size, sortBy, sortDirection, search, active))
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> findById(
            @PathVariable @Parameter(description = "ID del producto") Long id) {
        logger.info("Solicitud para obtener producto id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Producto obtenido correctamente", productService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    @Operation(summary = "Crear producto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos")
    })
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> create(
            @Valid @RequestBody @Parameter(description = "Datos del producto") ProductCreateDto request) {
        logger.info("Solicitud para crear producto");
        ProductResponseDto created = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Producto creado correctamente", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    @Operation(summary = "Actualizar producto")
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> update(
            @PathVariable @Parameter(description = "ID del producto") Long id,
            @Valid @RequestBody ProductUpdateDto request) {
        logger.info("Solicitud para actualizar producto id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Producto actualizado correctamente", productService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    @Operation(summary = "Eliminar producto")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable @Parameter(description = "ID del producto") Long id) {
        logger.info("Solicitud para eliminar producto id={}", id);
        productService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Producto eliminado correctamente", null));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    @Operation(summary = "Activar producto")
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> activate(
            @PathVariable @Parameter(description = "ID del producto") Long id) {
        logger.info("Solicitud para activar producto id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Producto activado correctamente", productService.activate(id)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    @Operation(summary = "Desactivar producto")
    public ResponseEntity<ApiResponseDto<Void>> deactivate(
            @PathVariable @Parameter(description = "ID del producto") Long id) {
        logger.info("Solicitud para desactivar producto id={}", id);
        productService.deactivate(id);
        return ResponseEntity.ok(ApiResponseDto.success("Producto desactivado correctamente", null));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "Obtener productos con bajo stock")
    public ResponseEntity<ApiResponseDto<List<ProductResponseDto>>> findLowStock() {
        logger.info("Solicitud para obtener productos con bajo stock");
        return ResponseEntity.ok(ApiResponseDto.success("Productos con bajo stock obtenidos correctamente", productService.findLowStock()));
    }
}
