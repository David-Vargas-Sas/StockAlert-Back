package com.stockalert.auth.dto;

import com.stockalert.users.dto.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private Long expiresInMinutes;
    private Long refreshTokenExpiresInDays;
    private UserResponseDto user;
}
