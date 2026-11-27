package com.proyecto.servicios.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "gestopago.tx")
public class GestoPagoTxProperties {

    @NotBlank
    private String unidad;

    @NotBlank
    private String apiKey;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal montoMinimo = new BigDecimal("1.00");

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal montoMaximo = new BigDecimal("10000.00");

    @Min(0)
    private int ventanaDuplicadoSegundos = 60;

    private Set<String> codigosPendientes = new HashSet<>();

    private boolean mockSandbox = false;

    @Valid
    private Conciliacion conciliacion = new Conciliacion();

    @Getter
    @Setter
    public static class Conciliacion {
        private boolean habilitada = true;
        @Min(1)
        private int esperaMinutos = 5;
        @Min(1)
        private int maxIntentos = 5;
        private boolean reenvioMismoUpc = false;
    }

    @Override
    public String toString() {
        return "GestoPagoTxProperties[unidad=" + unidad + ", apiKey=***]";
    }
}
