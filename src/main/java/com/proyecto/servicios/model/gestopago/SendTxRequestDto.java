package com.proyecto.servicios.model.gestopago;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendTxRequestDto {
    private String idServicio;
    private String idProducto;
    private String referencia;
    private String telefono;
    private BigDecimal monto;
    private String transaccionLocalId;
}

