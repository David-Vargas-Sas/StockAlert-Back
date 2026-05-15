package com.stockalert.security;

import com.stockalert.shared.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class TokenService {

    private final String secret;
    private final Long expirationMinutes;

    public TokenService(
            @Value("${stockalert.security.jwt-secret}") String secret,
            @Value("${stockalert.security.jwt-expiration-minutes}") Long expirationMinutes
    ) {
        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(UserPrincipal principal) {
        long expiresAt = Instant.now().plusSeconds(expirationMinutes * 60).getEpochSecond();
        String payload = principal.getUsername() + "|" + principal.getId() + "|" + principal.getCompanyId() + "|" + expiresAt;
        String encodedPayload = base64Url(payload);
        return encodedPayload + "." + sign(encodedPayload);
    }

    public String extractUsername(String token) {
        String[] parts = splitToken(token);
        validateSignature(parts[0], parts[1]);
        String[] payloadParts = decode(parts[0]).split("\\|");
        if (payloadParts.length != 4) {
            throw new BusinessException("Token invalido");
        }
        long expiresAt = Long.parseLong(payloadParts[3]);
        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new BusinessException("Token expirado");
        }
        return payloadParts[0];
    }

    public Long getExpirationMinutes() {
        return expirationMinutes;
    }

    private String[] splitToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new BusinessException("Token invalido");
        }
        return parts;
    }

    private void validateSignature(String payload, String signature) {
        if (!sign(payload).equals(signature)) {
            throw new BusinessException("Firma del token invalida");
        }
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new BusinessException("No fue posible firmar el token");
        }
    }

    private String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
