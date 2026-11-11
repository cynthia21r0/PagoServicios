CREATE TABLE IF NOT EXISTS clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    segundo_nombre VARCHAR(50),
    apellido_paterno VARCHAR(50) NOT NULL,
    apellido_materno VARCHAR(50) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    curp VARCHAR(18) NOT NULL,
    rfc VARCHAR(13) NOT NULL,
    sexo VARCHAR(30) NOT NULL,
    nacionalidad VARCHAR(50) NOT NULL,
    estado_civil VARCHAR(30) NOT NULL,
    correo_electronico VARCHAR(100) NOT NULL,
    telefono_movil VARCHAR(10) NOT NULL,
    telefono_alternativo VARCHAR(10),
    ocupacion VARCHAR(100) NOT NULL,
    empresa VARCHAR(100) NOT NULL,
    ingreso_mensual DECIMAL(19, 2) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_clientes_curp UNIQUE (curp),
    CONSTRAINT uq_clientes_rfc UNIQUE (rfc),
    CONSTRAINT uq_clientes_correo UNIQUE (correo_electronico),
    CONSTRAINT chk_clientes_ingreso_positivo CHECK (ingreso_mensual > 0),
    CONSTRAINT chk_clientes_curp_len CHECK (CHAR_LENGTH(curp) = 18),
    CONSTRAINT chk_clientes_rfc_len CHECK (CHAR_LENGTH(rfc) BETWEEN 12 AND 13),
    CONSTRAINT chk_clientes_tel_movil_len CHECK (CHAR_LENGTH(telefono_movil) = 10)
);

CREATE TABLE IF NOT EXISTS domicilios (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    calle VARCHAR(150) NOT NULL,
    numero_exterior VARCHAR(30) NOT NULL,
    numero_interior VARCHAR(30),
    colonia VARCHAR(100) NOT NULL,
    municipio VARCHAR(100) NOT NULL,
    estado VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(5) NOT NULL,
    pais VARCHAR(100) NOT NULL,

    CONSTRAINT uq_domicilios_cliente UNIQUE (cliente_id),
    CONSTRAINT fk_domicilios_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE,
    CONSTRAINT chk_domicilios_cp_len CHECK (CHAR_LENGTH(codigo_postal) = 5)
);

CREATE TABLE IF NOT EXISTS cuentas (
    id BIGSERIAL PRIMARY KEY,
    numero_cuenta VARCHAR(20) NOT NULL,
    saldo DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    estatus VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    cliente_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_cuentas_numero_cuenta UNIQUE (numero_cuenta),
    CONSTRAINT fk_cuentas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE,
    CONSTRAINT chk_cuentas_saldo_no_negativo CHECK (saldo >= 0)
);

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT,
    correo VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_usuarios_cliente UNIQUE (cliente_id),
    CONSTRAINT uq_usuarios_correo UNIQUE (correo),
    CONSTRAINT fk_usuarios_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_clientes_curp ON clientes(curp);
CREATE INDEX IF NOT EXISTS idx_clientes_rfc ON clientes(rfc);
CREATE INDEX IF NOT EXISTS idx_clientes_correo ON clientes(correo_electronico);
CREATE INDEX IF NOT EXISTS idx_clientes_nombre ON clientes(nombre);
CREATE INDEX IF NOT EXISTS idx_clientes_apellido_paterno ON clientes(apellido_paterno);
CREATE INDEX IF NOT EXISTS idx_clientes_apellido_materno ON clientes(apellido_materno);
CREATE INDEX IF NOT EXISTS idx_clientes_activo ON clientes(activo);
CREATE INDEX IF NOT EXISTS idx_clientes_fecha_creacion ON clientes(fecha_creacion);

CREATE INDEX IF NOT EXISTS idx_domicilios_cliente_id ON domicilios(cliente_id);
CREATE INDEX IF NOT EXISTS idx_domicilios_codigo_postal ON domicilios(codigo_postal);

CREATE INDEX IF NOT EXISTS idx_cuentas_numero_cuenta ON cuentas(numero_cuenta);
CREATE INDEX IF NOT EXISTS idx_cuentas_cliente_id ON cuentas(cliente_id);
CREATE INDEX IF NOT EXISTS idx_cuentas_estatus ON cuentas(estatus);

CREATE INDEX IF NOT EXISTS idx_usuarios_correo ON usuarios(correo);
CREATE INDEX IF NOT EXISTS idx_usuarios_cliente_id ON usuarios(cliente_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_activo ON usuarios(activo);
