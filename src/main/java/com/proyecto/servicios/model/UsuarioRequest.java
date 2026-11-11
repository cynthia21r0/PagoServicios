package com.proyecto.servicios.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequest {
    private Long clienteId;
    @JsonAlias({"correoElectronico", "email"})
    private String correo;
    private String password;
    private Boolean activo;
}

