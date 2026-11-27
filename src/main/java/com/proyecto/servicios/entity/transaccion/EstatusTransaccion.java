package com.proyecto.servicios.entity.transaccion;

public enum EstatusTransaccion {
    PENDIENTE,
    EXITOSA,
    RECHAZADA,
    FALLIDA,
    PENDIENTE_CONCILIACION;

    public boolean esFinal() {
        return this == EXITOSA || this == RECHAZADA || this == FALLIDA;
    }

    public boolean requiereConciliacion() {
        return this == PENDIENTE || this == PENDIENTE_CONCILIACION;
    }
}
