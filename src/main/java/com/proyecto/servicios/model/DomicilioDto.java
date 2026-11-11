package com.proyecto.servicios.model;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomicilioDto {

    private Long id;

    @Size(min = 2, max = 150, message = "La calle debe tener entre 2 y 150 caracteres")
    private String calle;

    @Size(min = 1, max = 30, message = "El número exterior debe tener entre 1 y 30 caracteres")
    private String numeroExterior;

    @Size(max = 30, message = "El número interior no puede exceder 30 caracteres")
    private String numeroInterior;

    @Size(min = 2, max = 100, message = "La colonia debe tener entre 2 y 100 caracteres")
    private String colonia;

    @Size(min = 2, max = 100, message = "El municipio debe tener entre 2 y 100 caracteres")
    private String municipio;

    @Size(min = 2, max = 100, message = "El estado debe tener entre 2 y 100 caracteres")
    private String estado;

    @Pattern(regexp = "^\\d{5}$", message = "El código postal debe contener exactamente 5 dígitos numéricos")
    private String codigoPostal;

    @Size(min = 2, max = 100, message = "El país debe tener entre 2 y 100 caracteres")
    private String pais;
}
