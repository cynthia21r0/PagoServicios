package com.proyecto.servicios.exception;

public class ProveedorNoDisponibleException extends RuntimeException {

    public ProveedorNoDisponibleException(String message) {
        super(message);
    }

    public ProveedorNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
