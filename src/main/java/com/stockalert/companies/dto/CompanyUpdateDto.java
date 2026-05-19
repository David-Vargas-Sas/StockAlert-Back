package com.stockalert.companies.dto;

import com.stockalert.companies.model.CompanyStatus;
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
public class CompanyUpdateDto {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(max = 150, message = "El nombre de la empresa no puede superar 150 caracteres")
    private String name;

    @Size(max = 150, message = "El nombre comercial no puede superar 150 caracteres")
    private String tradeName;

    @Size(max = 30, message = "El NIT no puede superar 30 caracteres")
    private String taxId;

    @Size(max = 5, message = "El digito de verificacion no puede superar 5 caracteres")
    private String verificationDigit;

    @Email(message = "El correo debe tener un formato valido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String email;

    @Size(max = 30, message = "El telefono no puede superar 30 caracteres")
    private String phone;

    @Size(max = 255, message = "La direccion no puede superar 255 caracteres")
    private String address;

    @Size(max = 100, message = "La ciudad no puede superar 100 caracteres")
    private String city;

    @Size(max = 100, message = "El departamento no puede superar 100 caracteres")
    private String department;

    @Size(max = 100, message = "El pais no puede superar 100 caracteres")
    private String country;

    @Size(max = 150, message = "El representante legal no puede superar 150 caracteres")
    private String legalRepresentative;

    @Size(max = 30, message = "El documento del representante legal no puede superar 30 caracteres")
    private String legalRepresentativeDocument;

    @Size(max = 255, message = "El sitio web no puede superar 255 caracteres")
    private String website;

    @Size(max = 255, message = "La ruta del logo no puede superar 255 caracteres")
    private String logoPath;

    private CompanyStatus status;
}
