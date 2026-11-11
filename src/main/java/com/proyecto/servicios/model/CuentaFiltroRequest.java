package com.proyecto.servicios.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaFiltroRequest {

    private Long id;

    @Size(max = 20, message = "El número de cuenta no puede exceder 20 caracteres")
    private String numeroCuenta;

    private Long clienteId;

    @Size(max = 20, message = "El estatus no puede exceder 20 caracteres")
    private String estatus;

    @DecimalMin(value = "0.00", message = "El saldo mínimo no puede ser negativo")
    private BigDecimal saldoMinimo;

    @DecimalMin(value = "0.00", message = "El saldo máximo no puede ser negativo")
    private BigDecimal saldoMaximo;
}

