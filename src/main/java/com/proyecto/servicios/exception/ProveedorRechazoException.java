package com.proyecto.servicios.exception;

public class ProveedorRechazoException extends OperacionNoProcesableException {

    private final String codigoProveedor;

    public ProveedorRechazoException(String codigoProveedor) {
        super("El proveedor rechazo la operacion. Codigo: " + codigoProveedor);
        this.codigoProveedor = codigoProveedor;
    }

    public String getCodigoProveedor() {
        return codigoProveedor;
    }
}