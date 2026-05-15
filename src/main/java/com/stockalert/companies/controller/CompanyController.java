package com.stockalert.companies.controller;

import com.stockalert.companies.dto.CompanyCreateDto;
import com.stockalert.companies.dto.CompanyResponseDto;
import com.stockalert.companies.dto.CompanyUpdateDto;
import com.stockalert.companies.service.CompanyService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@Tag(name = "Empresas", description = "Gestion de empresas")
public class CompanyController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMPANY_READ')")
    @Operation(summary = "Obtener todas las empresas")
    public ResponseEntity<ApiResponseDto<List<CompanyResponseDto>>> findAll() {
        logger.info("Solicitud para obtener empresas");
        return ResponseEntity.ok(ApiResponseDto.success("Empresas obtenidas correctamente", companyService.findAll()));
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAuthority('COMPANY_READ')")
    @Operation(summary = "Obtener empresas paginadas")
    public ResponseEntity<ApiResponseDto<PageResponseDto<CompanyResponseDto>>> findAllPaginated(
            @RequestParam(defaultValue = "0") @Parameter(description = "Numero de pagina") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Tamano de pagina") int size,
            @RequestParam(defaultValue = "id") @Parameter(description = "Campo por el que ordenar") String sortBy,
            @RequestParam(defaultValue = "asc") @Parameter(description = "Direccion de ordenamiento") String sortDirection) {
        logger.info("Solicitud paginada empresas - page: {}, size: {}, sortBy: {}, direction: {}", page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success(
                "Empresas paginadas obtenidas correctamente",
                PageResponseDto.from(companyService.findAllPaginated(page, size, sortBy, sortDirection))
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPANY_READ')")
    @Operation(summary = "Obtener empresa por ID")
    public ResponseEntity<ApiResponseDto<CompanyResponseDto>> findById(
            @PathVariable @Parameter(description = "ID de la empresa") Long id) {
        logger.info("Solicitud para obtener empresa id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Empresa obtenida correctamente", companyService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('COMPANY_CREATE')")
    @Operation(summary = "Crear empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empresa creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos")
    })
    public ResponseEntity<ApiResponseDto<CompanyResponseDto>> create(
            @Valid @RequestBody @Parameter(description = "Datos de la empresa") CompanyCreateDto request) {
        logger.info("Solicitud para crear empresa");
        CompanyResponseDto created = companyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Empresa creada correctamente", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPANY_UPDATE')")
    @Operation(summary = "Actualizar empresa")
    public ResponseEntity<ApiResponseDto<CompanyResponseDto>> update(
            @PathVariable @Parameter(description = "ID de la empresa") Long id,
            @Valid @RequestBody CompanyUpdateDto request) {
        logger.info("Solicitud para actualizar empresa id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Empresa actualizada correctamente", companyService.update(id, request)));
    }
}
