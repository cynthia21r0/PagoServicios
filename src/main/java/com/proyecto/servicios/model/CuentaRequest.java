package com.proyecto.servicios.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class CuentaRequest {

    private Long id;

    @NotNull(message = "El identificador del cliente (clienteId) es obligatorio")
    private Long clienteId;

    @DecimalMin(value = "0.00", message = "El saldo inicial no puede ser negativo")
    private BigDecimal saldo;

    @Size(min = 10, max = 20, message = "El número de cuenta debe tener entre 10 y 20 caracteres")
    private String numeroCuenta;

    @Pattern(regexp = "^(ACTIVA|INACTIVA|BLOQUEADA|CANCELADA)$", message = "El estatus debe ser ACTIVA, INACTIVA, BLOQUEADA o CANCELADA")
    private String estatus;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
