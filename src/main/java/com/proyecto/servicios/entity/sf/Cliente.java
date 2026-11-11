package com.proyecto.servicios.entity.sf;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "clientes",
        indexes = {
                @Index(name = "idx_clientes_curp", columnList = "curp", unique = true),
                @Index(name = "idx_clientes_rfc", columnList = "rfc", unique = true),
                @Index(name = "idx_clientes_correo", columnList = "correo_electronico", unique = true),
                @Index(name = "idx_clientes_nombre", columnList = "nombre"),
                @Index(name = "idx_clientes_apellido_paterno", columnList = "apellido_paterno"),
                @Index(name = "idx_clientes_apellido_materno", columnList = "apellido_materno"),
                @Index(name = "idx_clientes_activo", columnList = "activo"),
                @Index(name = "idx_clientes_fecha_creacion", columnList = "fecha_creacion")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(name = "segundo_nombre", length = 50)
    private String segundoNombre;

    @Column(name = "apellido_paterno", nullable = false, length = 50)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 50)
    private String apellidoMaterno;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false, unique = true, length = 18, updatable = false)
    private String curp;

    @Column(nullable = false, unique = true, length = 13, updatable = false)
    private String rfc;

    @Column(nullable = false, length = 30)
    private String sexo;

    @Column(nullable = false, length = 50)
    private String nacionalidad;

    @Column(name = "estado_civil", nullable = false, length = 30)
    private String estadoCivil;

    @Column(name = "correo_electronico", nullable = false, unique = true, length = 100)
    private String correoElectronico;

    @Column(name = "telefono_movil", nullable = false, length = 10)
    private String telefonoMovil;

    @Column(name = "telefono_alternativo", length = 10)
    private String telefonoAlternativo;

    @Column(nullable = false, length = 100)
    private String ocupacion;

    @Column(nullable = false, length = 100)
    private String empresa;

    @Column(name = "ingreso_mensual", nullable = false, precision = 19, scale = 2)
    private BigDecimal ingresoMensual;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Domicilio domicilio;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Cuenta> cuentas = new ArrayList<>();

    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Usuario usuario;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.fechaCreacion == null) {
            this.fechaCreacion = now;
        }
        this.fechaActualizacion = now;
        if (this.activo == null) {
            this.activo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
