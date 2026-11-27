package com.proyecto.servicios.service.pago;

public record ResultadoProveedor(Tipo tipo, String codigo, String texto, String idTx, String numAutorizacion) {

    public enum Tipo {
        EXITO,
        RECHAZO,
        NO_ENVIADO,
        INCIERTO
    }

    public static ResultadoProveedor exito(String codigo, String texto, String idTx, String numAutorizacion) {
        return new ResultadoProveedor(Tipo.EXITO, codigo, texto, idTx, numAutorizacion);
    }

    public static ResultadoProveedor rechazo(String codigo, String texto, String idTx, String numAutorizacion) {
        return new ResultadoProveedor(Tipo.RECHAZO, codigo, texto, idTx, numAutorizacion);
    }

    public static ResultadoProveedor noEnviado(String motivo) {
        return new ResultadoProveedor(Tipo.NO_ENVIADO, "NO_ENVIADO", motivo, null, null);
    }

    public static ResultadoProveedor incierto(String codigo, String motivo) {
        return new ResultadoProveedor(Tipo.INCIERTO, codigo, motivo, null, null);
    }
}