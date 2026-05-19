package com.stockalert.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateDto {

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 100, message = "El nombre del rol no puede superar 100 caracteres")
    private String name;

    @Size(max = 255, message = "La descripcion del rol no puede superar 255 caracteres")
    private String description;

    private Boolean active;

    private Set<Long> permissionIds;
}
