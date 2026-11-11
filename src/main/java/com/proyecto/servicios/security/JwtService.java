package com.proyecto.servicios.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.proyecto.servicios.entity.sf.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret:OnboardingPersonasFisicasSecretKey2026FinancieraJwtTokenSeguridad}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);
        Algorithm algorithm = Algorithm.HMAC256(secret);

        var builder = JWT.create()
                .withSubject(usuario.getCorreo())
                .withClaim("usuarioId", usuario.getId())
                .withClaim("correo", usuario.getCorreo())
                .withClaim("activo", usuario.getActivo())
                .withIssuedAt(ahora)
                .withExpiresAt(expiracion);

        if (usuario.getCliente() != null && usuario.getCliente().getId() != null) {
            builder.withClaim("clienteId", usuario.getCliente().getId());
        }

        return builder.sign(algorithm);
    }

    public DecodedJWT validarToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        } catch (JWTVerificationException ex) {
            return null;
        }
    }

    public String obtenerCorreoDeToken(String token) {
        DecodedJWT decoded = validarToken(token);
        return decoded != null ? decoded.getSubject() : null;
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}

