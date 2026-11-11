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
public class PagoServicioResponse {

    private Long transaccionId;
    private String idempotencyKey;
    private String transaccionLocalId;
    private Long clienteId;
    private String numeroCuenta;
    private String idServicio;
    private String idProducto;
    private String nombreProducto;
    private String referenciaPago;
    private BigDecimal monto;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoDisponible;
    private String estatus;
    private String folioProveedor;
    private String codigoRespuestaProveedor;
    private String mensajeOperacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}

