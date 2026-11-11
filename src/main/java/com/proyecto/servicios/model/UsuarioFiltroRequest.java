package com.proyecto.servicios.model;

import com.fasterxml.jackson.annotation.JsonAlias;
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
public class UsuarioFiltroRequest {

    private Long id;

    private Long clienteId;

    @JsonAlias({"correoElectronico", "email"})
    @Size(max = 100, message = "El criterio de correo no puede exceder 100 caracteres")
    private String correo;

    private Boolean activo;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;
}

