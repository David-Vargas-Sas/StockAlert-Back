package com.stockalert.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockalert.shared.response.ApiErrorDto;
import com.stockalert.shared.response.ApiResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        ApiErrorDto error = ApiErrorDto.builder()
                .status(HttpServletResponse.SC_FORBIDDEN)
                .error("Forbidden")
                .path(request.getRequestURI())
                .build();
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponseDto.error("No tienes permisos para realizar esta accion", error)));
    }
}
