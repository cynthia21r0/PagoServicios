package com.proyecto.servicios.entity.sf;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transacciones_pago",
        indexes = {
                @Index(name = "idx_tx_pago_idempotency", columnList = "idempotency_key", unique = true),
                @Index(name = "idx_tx_pago_local_id", columnList = "transaccion_local_id", unique = true),
                @Index(name = "idx_tx_pago_cliente_id", columnList = "cliente_id"),
                @Index(name = "idx_tx_pago_numero_cuenta", columnList = "numero_cuenta"),
                @Index(name = "idx_tx_pago_estatus", columnList = "estatus"),
                @Index(name = "idx_tx_pago_fecha_creacion", columnList = "fecha_creacion")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class TransaccionPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100, updatable = false)
    private String idempotencyKey;

    @Column(name = "transaccion_local_id", nullable = false, unique = true, length = 60, updatable = false)
    private String transaccionLocalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    @Column(name = "numero_cuenta", nullable = false, length = 20)
    private String numeroCuenta;

    @Column(name = "id_servicio", nullable = false, length = 50)
    private String idServicio;

    @Column(name = "id_producto", nullable = false, length = 50)
    private String idProducto;

    @Column(name = "nombre_producto", length = 150)
    private String nombreProducto;

    @Column(name = "referencia_pago", nullable = false, length = 100)
    private String referenciaPago;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(name = "saldo_anterior", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_posterior", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoPosterior;

    @Column(nullable = false, length = 40)
    private String estatus;

    @Column(name = "folio_proveedor", length = 100)
    private String folioProveedor;

    @Column(name = "codigo_respuesta_proveedor", length = 50)
    private String codigoRespuestaProveedor;

    @Column(name = "mensaje_proveedor", length = 255)
    private String mensajeProveedor;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.fechaCreacion == null) {
            this.fechaCreacion = now;
        }
        this.fechaActualizacion = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}

