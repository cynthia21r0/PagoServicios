package com.proyecto.servicios.model;

import jakarta.validation.constraints.NotBlank;
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
public class ConciliacionTransaccionRequest {

    @NotBlank(message = "El identificador local de la transacción (transaccionLocalId) es obligatorio")
    @Size(max = 60, message = "El identificador de transacción no puede exceder 60 caracteres")
    private String transaccionLocalId;

    @NotBlank(message = "La resolución de conciliación (resolucionProveedor) es obligatoria")
    @Pattern(regexp = "^(CONFIRMAR_EXITOSA|REVERSAR_SALDO)$", message = "La resolución debe ser CONFIRMAR_EXITOSA o REVERSAR_SALDO")
    private String resolucionProveedor;

    @Size(max = 100, message = "El folio del proveedor no puede exceder 100 caracteres")
    private String folioProveedor;

    @Size(max = 255, message = "El motivo de conciliación no puede exceder 255 caracteres")
    private String motivo;
}

