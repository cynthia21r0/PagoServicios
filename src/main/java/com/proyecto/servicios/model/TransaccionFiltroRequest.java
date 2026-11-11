package com.proyecto.servicios.model;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionFiltroRequest {

    private Long clienteId;

    @Size(max = 20, message = "El número de cuenta no puede exceder 20 caracteres")
    private String numeroCuenta;

    @Size(max = 40, message = "El estatus no puede exceder 40 caracteres")
    private String estatus;

    @Size(max = 50, message = "El idServicio no puede exceder 50 caracteres")
    private String idServicio;

    @Size(max = 50, message = "El idProducto no puede exceder 50 caracteres")
    private String idProducto;

    @Size(max = 100, message = "El idempotencyKey no puede exceder 100 caracteres")
    private String idempotencyKey;

    @Size(max = 60, message = "El transaccionLocalId no puede exceder 60 caracteres")
    private String transaccionLocalId;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;
}

