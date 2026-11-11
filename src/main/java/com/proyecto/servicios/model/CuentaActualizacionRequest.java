package com.proyecto.servicios.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaActualizacionRequest {
    private BigDecimal saldo;
    private String estatus;
    private String numeroCuenta;
}
