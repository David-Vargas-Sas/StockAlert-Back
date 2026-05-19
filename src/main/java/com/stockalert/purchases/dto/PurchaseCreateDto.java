package com.stockalert.purchases.dto;

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
public class PurchaseCreateDto {

    private Long supplierId;

    @Valid
    @NotEmpty(message = "La compra debe tener al menos un producto")
    private List<PurchaseItemCreateDto> items;
}
