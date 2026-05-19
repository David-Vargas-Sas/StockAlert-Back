package com.stockalert.auth.service;

import com.stockalert.auth.dto.AuthResponseDto;
import com.stockalert.auth.dto.LoginRequestDto;
import com.stockalert.auth.dto.LogoutRequestDto;
import com.stockalert.auth.dto.LogoutResponseDto;
import com.stockalert.auth.dto.RefreshTokenRequestDto;
import com.stockalert.auth.model.RefreshToken;
import com.stockalert.auth.model.SessionEventType;
import com.stockalert.auth.model.SessionLog;
import com.stockalert.auth.repository.RefreshTokenRepository;
import com.stockalert.auth.repository.SessionLogRepository;
import com.stockalert.security.TokenService;
import com.stockalert.security.UserPrincipal;
import com.stockalert.shared.exception.BusinessException;
import com.stockalert.users.model.User;
import com.stockalert.users.repository.UserRepository;
import com.stockalert.users.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SessionLogRepository sessionLogRepository;

    @Value("${stockalert.security.refresh-token-expiration-days}")
    private Long refreshTokenExpirationDays;

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    @Transactional(noRollbackFor = BadCredentialsException.class)
    public AuthResponseDto login(LoginRequestDto request, HttpServletRequest httpRequest) {
        User userForLockValidation = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (userForLockValidation != null) {
            validateUserCanStartSession(userForLockValidation);
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException exception) {
            registerFailedLogin(request.getUsername(), getClientIp(httpRequest));
            throw exception;
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userService.findEntityByUsername(principal.getUsername());
        validateUserCanStartSession(user);
        resetFailedLogin(user);
        closeActiveSessions(user, getClientIp(httpRequest), "Sesion cerrada por nuevo inicio de sesion");
        RefreshToken refreshToken = createRefreshToken(user);
        logSession(user, SessionEventType.LOGIN, getClientIp(httpRequest), "Login exitoso");

        return AuthResponseDto.builder()
                .tokenType("Bearer")
                .accessToken(tokenService.generateToken(principal))
                .refreshToken(refreshToken.getToken())
                .expiresInMinutes(tokenService.getExpirationMinutes())
                .refreshTokenExpiresInDays(refreshTokenExpirationDays)
                .user(userService.toResponse(user))
                .build();
    }

    @Transactional
    public AuthResponseDto refresh(RefreshTokenRequestDto request, HttpServletRequest httpRequest) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Refresh token invalido"));
        if (Boolean.TRUE.equals(refreshToken.getRevoked()) || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Refresh token expirado o revocado");
        }

        User user = refreshToken.getUser();
        validateUserCanStartSession(user);
        UserPrincipal principal = new UserPrincipal(user);
        revokeRefreshToken(refreshToken);
        RefreshToken newRefreshToken = createRefreshToken(user);
        logSession(user, SessionEventType.REFRESH, getClientIp(httpRequest), "Refresh token utilizado");

        return AuthResponseDto.builder()
                .tokenType("Bearer")
                .accessToken(tokenService.generateToken(principal))
                .refreshToken(newRefreshToken.getToken())
                .expiresInMinutes(tokenService.getExpirationMinutes())
                .refreshTokenExpiresInDays(refreshTokenExpirationDays)
                .user(userService.toResponse(user))
                .build();
    }

    @Transactional
    public LogoutResponseDto logout(LogoutRequestDto request, HttpServletRequest httpRequest) {
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            refreshTokenRepository.findByToken(request.getRefreshToken())
                    .ifPresent(refreshToken -> {
                        revokeRefreshToken(refreshToken);
                        logSession(refreshToken.getUser(), SessionEventType.LOGOUT, getClientIp(httpRequest), "Logout con refresh token revocado");
                    });
        } else {
            closeAuthenticatedUserSessions(httpRequest);
        }
        return LogoutResponseDto.builder()
                .message("Sesion cerrada correctamente")
                .build();
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(generateSecureToken())
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    private void closeActiveSessions(User user, String ipAddress, String message) {
        refreshTokenRepository.findByUserIdAndRevokedFalseAndExpiresAtAfter(user.getId(), LocalDateTime.now())
                .forEach(refreshToken -> {
                    revokeRefreshToken(refreshToken);
                    logSession(user, SessionEventType.LOGOUT, ipAddress, message);
                });
    }

    private void closeAuthenticatedUserSessions(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            User user = userService.findEntityByUsername(principal.getUsername());
            closeActiveSessions(user, getClientIp(request), "Logout de todas las sesiones activas");
        }
    }

    private void revokeRefreshToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
    }

    private void validateUserCanStartSession(User user) {
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BusinessException("Usuario inactivo");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Usuario bloqueado temporalmente hasta: " + user.getLockedUntil());
        }
    }

    private void registerFailedLogin(String username, String ipAddress) {
        userRepository.findByUsername(username).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() == null ? 1 : user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
                user.setFailedLoginAttempts(0);
                logSession(user, SessionEventType.LOGIN, ipAddress, "Usuario bloqueado por intentos fallidos");
            }
        });
    }

    private void resetFailedLogin(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void logSession(User user, SessionEventType eventType, String ipAddress, String message) {
        sessionLogRepository.save(SessionLog.builder()
                .user(user)
                .company(user.getCompany())
                .username(user.getUsername())
                .eventType(eventType)
                .ipAddress(ipAddress)
                .message(message)
                .build());
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
