package com.proyecto.servicios.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteFiltroRequest {

    private Long id;

    @Size(max = 50, message = "El nombre de búsqueda no puede exceder 50 caracteres")
    private String nombre;

    @Size(max = 50, message = "El segundo nombre de búsqueda no puede exceder 50 caracteres")
    private String segundoNombre;

    @Size(max = 50, message = "El apellido paterno de búsqueda no puede exceder 50 caracteres")
    private String apellidoPaterno;

    @Size(max = 50, message = "El apellido materno de búsqueda no puede exceder 50 caracteres")
    private String apellidoMaterno;

    @Pattern(regexp = "^[A-Za-z]{4}\\d{6}[HMhm][A-Za-z]{5}[A-Za-z0-9]\\d$", message = "La CURP de búsqueda no tiene un formato válido")
    private String curp;

    @Pattern(regexp = "^[A-Za-zÑñ&]{3,4}\\d{6}[A-Za-z0-9]{3}$", message = "El RFC de búsqueda no tiene un formato válido")
    private String rfc;

    @JsonAlias({"correo"})
    @Email(message = "El correo electrónico de búsqueda debe tener un formato válido")
    @Size(max = 100, message = "El correo electrónico no puede exceder 100 caracteres")
    private String correoElectronico;

    @Size(max = 20, message = "El número de cuenta no puede exceder 20 caracteres")
    private String numeroCuenta;

    private Boolean activo;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;
}

