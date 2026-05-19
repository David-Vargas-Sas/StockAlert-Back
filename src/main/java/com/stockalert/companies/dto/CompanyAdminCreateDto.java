package com.stockalert.companies.dto;

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
public class CompanyAdminCreateDto {

    @NotBlank(message = "El usuario es obligatorio")
    @Size(max = 100, message = "El usuario no puede superar 100 caracteres")
    private String username;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 6, max = 100, message = "La contrasena debe tener entre 6 y 100 caracteres")
    private String password;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 150, message = "El nombre completo no puede superar 150 caracteres")
    private String fullName;
}
