package com.proyecto.servicios.entity.sf;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "domicilios",
        indexes = {
                @Index(name = "idx_domicilios_cliente_id", columnList = "cliente_id", unique = true),
                @Index(name = "idx_domicilios_codigo_postal", columnList = "codigo_postal")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Domicilio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", referencedColumnName = "id", nullable = false, unique = true)
    private Cliente cliente;

    @Column(nullable = false, length = 150)
    private String calle;

    @Column(name = "numero_exterior", nullable = false, length = 30)
    private String numeroExterior;

    @Column(name = "numero_interior", length = 30)
    private String numeroInterior;

    @Column(nullable = false, length = 100)
    private String colonia;

    @Column(nullable = false, length = 100)
    private String municipio;

    @Column(nullable = false, length = 100)
    private String estado;

    @Column(name = "codigo_postal", nullable = false, length = 5)
    private String codigoPostal;

    @Column(nullable = false, length = 100)
    private String pais;
}

