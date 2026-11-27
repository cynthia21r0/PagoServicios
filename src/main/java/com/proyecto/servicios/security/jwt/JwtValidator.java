package com.proyecto.servicios.security.jwt;

import com.proyecto.servicios.service.AuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtValidator {

    private final KeycloakJwtDecoder jwtDecoder;
    private final AuthService authService;

    public boolean validate(String token, UserDetails userDetails, String requestDeviceId) {
        if (token == null || token.isBlank()) {
            throw new TokenValidationException("El token proporcionado es nulo o vacío");
        }

        try {
            Claims claims = jwtDecoder.decodeClaims(token);
            String username = claims.getSubject();

            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.toInstant().isBefore(Instant.now())) {
                log.warn("[JWT EXPIRADO] El token para {} expiró en {}", username, expiration);
                throw new TokenValidationException("El token ha expirado");
            }

            if (userDetails != null && !username.equalsIgnoreCase(userDetails.getUsername())) {
                log.warn("[JWT USUARIO NO COINCIDE] Token: {}, UserDetails: {}", username, userDetails.getUsername());
                throw new TokenValidationException("El token no corresponde al usuario autenticado");
            }

            String sessionId = claims.get("sessionId", String.class);
            if (sessionId == null || sessionId.isBlank()) {
                throw new TokenValidationException("El token no corresponde a una sesión activa");
            }
            String tokenDeviceId = claims.get("deviceId", String.class);
            String deviceIdToVerify = (requestDeviceId != null && !requestDeviceId.isBlank()) ? requestDeviceId : tokenDeviceId;

            authService.validarSesionActiva(username, sessionId, deviceIdToVerify);

            return true;
        } catch (TokenValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("[ERROR VALIDACION JWT] {}", ex.getMessage());
            throw new TokenValidationException("Fallo en la validación del token: " + ex.getMessage(), ex);
        }
    }
}
