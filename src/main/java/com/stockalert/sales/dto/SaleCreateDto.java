package com.stockalert.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreateDto {

    @Valid
    @NotEmpty(message = "La venta debe tener al menos un producto")
    private List<SaleItemCreateDto> items;
}
