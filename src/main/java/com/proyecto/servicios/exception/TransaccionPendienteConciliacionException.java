package com.proyecto.servicios.exception;

import lombok.Getter;

@Getter
public class TransaccionPendienteConciliacionException extends RuntimeException {
    private final String transaccionLocalId;

    public TransaccionPendienteConciliacionException(String message, String transaccionLocalId) {
        super(message);
        this.transaccionLocalId = transaccionLocalId;
    }
}

