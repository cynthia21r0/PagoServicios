package com.proyecto.servicios.model.gestopago;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "RESPONSE")
@XmlAccessorType(XmlAccessType.FIELD)
public class SendTxResponseDto {

    @XmlElement(name = "CODIGO")
    private String codigo;

    @XmlElement(name = "MENSAJE")
    private String mensaje;

    @XmlElement(name = "ID_TX")
    private String idTransaccionProveedor;

    @XmlElement(name = "NUM_AUTORIZACION")
    private String numAutorizacion;

    private boolean aprobada;
    private boolean rechazoDefinitivo;
    private boolean estadoIndeterminado;
}

