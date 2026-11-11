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
public class ClienteBajaRequest {

    @NotNull(message = "El identificador del cliente (id) es obligatorio para realizar la baja lógica")
    private Long id;
}

