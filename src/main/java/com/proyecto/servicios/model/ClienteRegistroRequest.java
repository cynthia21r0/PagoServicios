package com.proyecto.servicios.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRegistroRequest {

    private String nombre;
    private String segundoNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private LocalDate fechaNacimiento;
    private String curp;
    private String rfc;
    private String sexo;
    private String nacionalidad;
    private String estadoCivil;

    private String correoElectronico;
    private String telefonoMovil;
    private String telefonoAlternativo;

    private DomicilioDto domicilio;
    private String calle;
    private String numeroExterior;
    private String numeroInterior;
    private String colonia;
    private String municipio;
    private String estado;
    private String codigoPostal;
    private String pais;

    private String ocupacion;
    private String empresa;
    private BigDecimal ingresoMensual;

    private String password;

    private BigDecimal saldoInicial;
}
