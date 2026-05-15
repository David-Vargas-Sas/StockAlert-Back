package com.stockalert.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long id;
    private Long companyId;
    private String companyName;
    private String username;
    private String email;
    private String fullName;
    private Boolean active;
    private LocalDateTime createdAt;
    private Set<RoleResponseDto> roles;
}
