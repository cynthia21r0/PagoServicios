package com.proyecto.servicios.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioBajaRequest {

    @NotNull(message = "El identificador del usuario (id) es obligatorio para dar de baja")
    private Long id;
}

