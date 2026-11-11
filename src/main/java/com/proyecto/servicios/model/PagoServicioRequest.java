package com.proyecto.servicios.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoServicioRequest {

    @NotBlank(message = "La clave de idempotencia (idempotencyKey) es obligatoria para evitar cargos duplicados")
    @Size(min = 6, max = 100, message = "La clave de idempotencia debe tener entre 6 y 100 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9\\-_:.]{6,100}$", message = "La clave de idempotencia contiene caracteres inválidos")
    private String idempotencyKey;

    @NotNull(message = "El identificador del cliente (clienteId) es obligatorio")
    private Long clienteId;

    @NotBlank(message = "El número de cuenta (numeroCuenta) es obligatorio")
    @Pattern(regexp = "^\\d{10,20}$", message = "El número de cuenta debe contener entre 10 y 20 dígitos numéricos")
    private String numeroCuenta;

    @NotBlank(message = "El identificador del servicio (idServicio) es obligatorio")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]{1,50}$", message = "El identificador del servicio no tiene un formato válido")
    private String idServicio;

    @NotBlank(message = "El identificador del producto (idProducto) es obligatorio")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]{1,50}$", message = "El identificador del producto no tiene un formato válido")
    private String idProducto;

    @NotBlank(message = "La referencia de pago (referenciaPago) es obligatoria")
    @Size(min = 4, max = 100, message = "La referencia de pago debe tener entre 4 y 100 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9\\-_ ]{4,100}$", message = "La referencia de pago solo permite caracteres alfanuméricos, espacios y guiones")
    private String referenciaPago;

    @Pattern(regexp = "^\\d{10}$", message = "El teléfono destino debe contener exactamente 10 dígitos numéricos")
    private String telefonoDestino;

    @NotNull(message = "El monto de la operación es obligatorio")
    @DecimalMin(value = "1.00", message = "El monto de la operación debe ser de al menos 1.00")
    @Digits(integer = 15, fraction = 2, message = "El monto debe tener máximo 2 decimales")
    private BigDecimal monto;
}

