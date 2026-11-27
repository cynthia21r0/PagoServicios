package com.proyecto.servicios.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtSecurityProperties {

    private String secretKey;

    private String issuer = "gesto-pagos-auth";

    private long expirationMs = 36000000L;

    private String keycloakServerUrl = "http://localhost:8081";

    private String keycloakRealm = "master";
}
