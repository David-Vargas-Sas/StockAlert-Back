package com.stockalert.suppliers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierCreateDto {

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(max = 150, message = "El nombre del proveedor no puede superar 150 caracteres")
    private String name;

    @Size(max = 30, message = "El NIT no puede superar 30 caracteres")
    private String taxId;

    @Size(max = 150, message = "El contacto no puede superar 150 caracteres")
    private String contactName;

    @Email(message = "El correo debe tener un formato valido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String email;

    @Size(max = 30, message = "El telefono no puede superar 30 caracteres")
    private String phone;

    @Size(max = 255, message = "La direccion no puede superar 255 caracteres")
    private String address;
}
