package com.proyecto.servicios.model.transaccion;

import com.proyecto.servicios.entity.transaccion.EstatusTransaccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionHistorialDTO {

    private Long folioTransaccion;
    private String upc;
    private String producto;
    private BigDecimal monto;
    private EstatusTransaccion estatus;
    private String numAutorizacion;
    private LocalDateTime fecha;
}