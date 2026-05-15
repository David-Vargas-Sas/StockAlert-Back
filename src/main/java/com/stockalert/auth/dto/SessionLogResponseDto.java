package com.stockalert.auth.dto;

import com.stockalert.auth.model.SessionEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionLogResponseDto {

    private Long id;
    private Long userId;
    private Long companyId;
    private String companyName;
    private String username;
    private SessionEventType eventType;
    private String ipAddress;
    private String message;
    private LocalDateTime createdAt;
}
