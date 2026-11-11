  CREATE TABLE personas_fisicas (
      id SERIAL PRIMARY KEY,
      nombre VARCHAR(255) NOT NULL,
      segundo_nombre VARCHAR(255),
      apellido_paterno VARCHAR(255) NOT NULL,
      apellido_materno VARCHAR(255) NOT NULL,
      fecha_nacimiento DATE NOT NULL,
      curp VARCHAR(18) NOT NULL UNIQUE,
      rfc VARCHAR(13) NOT NULL UNIQUE,
      sexo VARCHAR(255) NOT NULL,
      nacionalidad VARCHAR(255) NOT NULL,
      estado_civil VARCHAR(255) NOT NULL,
      correo_electronico VARCHAR(255) NOT NULL,
      telefono_movil VARCHAR(255) NOT NULL,
      telefono_alternativo VARCHAR(255),
      calle VARCHAR(255) NOT NULL,
      numero_exterior VARCHAR(255) NOT NULL,
      numero_interior VARCHAR(255),
      colonia VARCHAR(255) NOT NULL,
      municipio VARCHAR(255) NOT NULL,
      estado VARCHAR(255) NOT NULL,
      codigo_postal VARCHAR(255) NOT NULL,
      pais VARCHAR(255) NOT NULL,
      ocupacion VARCHAR(255) NOT NULL,
      empresa VARCHAR(255) NOT NULL,
      ingreso_mensual DECIMAL(19, 2) NOT NULL
  );

  CREATE TABLE cuentas_bancarias (
      id SERIAL PRIMARY KEY,
      numero_cuenta VARCHAR(255) NOT NULL UNIQUE,
      saldo DECIMAL(19, 2) NOT NULL,
      persona_id BIGINT NOT NULL,
      CONSTRAINT fk_persona_cuenta FOREIGN KEY (persona_id) REFERENCES personas_fisicas(id) ON DELETE CASCADE
  );