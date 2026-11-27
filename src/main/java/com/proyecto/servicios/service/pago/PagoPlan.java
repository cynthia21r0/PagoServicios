package com.proyecto.servicios.service.pago;

import com.proyecto.servicios.entity.gestopago.CatalogoProducto;

import java.math.BigDecimal;

public record PagoPlan(CatalogoProducto producto,
                       BigDecimal monto,
                       String telefono,
                       String referencia,
                       boolean montoAbierto) {
}