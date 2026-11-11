package com.proyecto.servicios.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaActualizacionRequest {

    private Long id;

    @NotBlank(message = "El número de cuenta (numeroCuenta) es obligatorio para identificar la cuenta")
    private String numeroCuenta;

    private String nuevoNumeroCuenta;

    @DecimalMin(value = "0.00", message = "El saldo de la cuenta no puede ser negativo")
    private BigDecimal saldo;

    @Pattern(regexp = "^(ACTIVA|INACTIVA|BLOQUEADA|CANCELADA)$", message = "El estatus debe ser ACTIVA, INACTIVA, BLOQUEADA o CANCELADA")
    private String estatus;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
