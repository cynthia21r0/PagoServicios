package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.TransaccionPago;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransaccionPagoRepository extends JpaRepository<TransaccionPago, Long>, JpaSpecificationExecutor<TransaccionPago> {

    Optional<TransaccionPago> findByIdempotencyKey(String idempotencyKey);

    Optional<TransaccionPago> findByTransaccionLocalId(String transaccionLocalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TransaccionPago t WHERE t.transaccionLocalId = :transaccionLocalId")
    Optional<TransaccionPago> findByTransaccionLocalIdForUpdate(@Param("transaccionLocalId") String transaccionLocalId);

    boolean existsByIdempotencyKey(String idempotencyKey);
}

