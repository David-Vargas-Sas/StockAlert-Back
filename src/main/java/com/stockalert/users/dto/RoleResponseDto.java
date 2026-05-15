package com.stockalert.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseDto {

    private Long id;
    private Long companyId;
    private String name;
    private String description;
    private Set<PermissionResponseDto> permissions;
}
