package com.proyecto.servicios.model;

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
public class CuentaResponse {
    private Long id;
    private String numeroCuenta;
    private BigDecimal saldo;
    private String estatus;
    private Long clienteId;
    private String nombreCliente;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}

