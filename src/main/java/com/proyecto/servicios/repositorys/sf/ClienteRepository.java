package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    Optional<Cliente> findByCurpIgnoreCase(String curp);

    Optional<Cliente> findByRfcIgnoreCase(String rfc);

    Optional<Cliente> findByCorreoElectronicoIgnoreCase(String correoElectronico);

    boolean existsByCurpIgnoreCase(String curp);

    boolean existsByRfcIgnoreCase(String rfc);

    boolean existsByCorreoElectronicoIgnoreCase(String correoElectronico);

    List<Cliente> findByNombreContainingIgnoreCase(String nombre);

    List<Cliente> findByApellidoPaternoContainingIgnoreCase(String apellidoPaterno);

    List<Cliente> findByApellidoMaternoContainingIgnoreCase(String apellidoMaterno);

    List<Cliente> findByActivoTrue();

    List<Cliente> findByActivo(Boolean activo);

    List<Cliente> findByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    @Query("SELECT DISTINCT c FROM Cliente c JOIN c.cuentas cta WHERE cta.numeroCuenta = :numeroCuenta")
    Optional<Cliente> findByNumeroCuenta(@Param("numeroCuenta") String numeroCuenta);
}
