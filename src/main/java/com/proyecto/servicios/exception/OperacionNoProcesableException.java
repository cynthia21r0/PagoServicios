package com.proyecto.servicios.exception;

public class OperacionNoProcesableException extends RuntimeException {
    public OperacionNoProcesableException(String message) {
        super(message);
    }
}