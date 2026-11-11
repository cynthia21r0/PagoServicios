CREATE TABLE IF NOT EXISTS transacciones_pago (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(100) NOT NULL,
    transaccion_local_id VARCHAR(60) NOT NULL,
    cliente_id BIGINT NOT NULL,
    cuenta_id BIGINT NOT NULL,
    numero_cuenta VARCHAR(20) NOT NULL,
    id_servicio VARCHAR(50) NOT NULL,
    id_producto VARCHAR(50) NOT NULL,
    nombre_producto VARCHAR(150),
    referencia_pago VARCHAR(100) NOT NULL,
    monto DECIMAL(19, 2) NOT NULL,
    saldo_anterior DECIMAL(19, 2) NOT NULL,
    saldo_posterior DECIMAL(19, 2) NOT NULL,
    estatus VARCHAR(40) NOT NULL,
    folio_proveedor VARCHAR(100),
    codigo_respuesta_proveedor VARCHAR(50),
    mensaje_proveedor VARCHAR(255),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_tx_pago_idempotency UNIQUE (idempotency_key),
    CONSTRAINT uq_tx_pago_local_id UNIQUE (transaccion_local_id),
    CONSTRAINT fk_tx_pago_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_tx_pago_cuenta FOREIGN KEY (cuenta_id) REFERENCES cuentas(id),
    CONSTRAINT chk_tx_pago_monto_positivo CHECK (monto > 0),
    CONSTRAINT chk_tx_pago_saldo_posterior CHECK (saldo_posterior >= 0)
);

CREATE INDEX IF NOT EXISTS idx_tx_pago_cliente_id ON transacciones_pago(cliente_id);
CREATE INDEX IF NOT EXISTS idx_tx_pago_numero_cuenta ON transacciones_pago(numero_cuenta);
CREATE INDEX IF NOT EXISTS idx_tx_pago_estatus ON transacciones_pago(estatus);
CREATE INDEX IF NOT EXISTS idx_tx_pago_fecha_creacion ON transacciones_pago(fecha_creacion);

