package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.entity.sf.TransaccionPago;
import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.model.ConciliacionTransaccionRequest;
import com.proyecto.servicios.model.PagoServicioRequest;
import com.proyecto.servicios.model.gestopago.SendTxResponseDto;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.repositorys.sf.TransaccionPagoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
public class TransaccionLedgerService {

    @Autowired
    private TransaccionPagoRepository transaccionPagoRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransaccionPago reservarSaldoYRegistrarTransaccion(PagoServicioRequest request, String nombreProducto, Usuario usuarioAutenticado) {
        String idempotencyKey = request.getIdempotencyKey().trim();
        if (transaccionPagoRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new TransaccionDuplicadaException(
                    "Ya existe una operación registrada con la clave de idempotencia proporcionada. No se procesó un cargo duplicado.");
        }

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontró el cliente especificado."));

        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new OperacionNoAutorizadaException("El cliente no se encuentra activo para realizar operaciones financieras.");
        }

        if (usuarioAutenticado != null) {
            if (!Boolean.TRUE.equals(usuarioAutenticado.getActivo())) {
                throw new OperacionNoAutorizadaException("El usuario autenticado se encuentra inactivo.");
            }
            if (usuarioAutenticado.getCliente() != null
                    && !usuarioAutenticado.getCliente().getId().equals(cliente.getId())) {
                throw new OperacionNoAutorizadaException(
                        "El usuario autenticado no tiene autorización para operar en nombre del cliente solicitado.");
            }
        }

        Cuenta cuenta = cuentaRepository.findByNumeroCuentaForUpdate(request.getNumeroCuenta().trim())
                .orElseThrow(() -> new CuentaNoEncontradaException("No se encontró la cuenta bancaria especificada."));

        if (cuenta.getCliente() == null || !cuenta.getCliente().getId().equals(cliente.getId())) {
            throw new OperacionNoAutorizadaException("La cuenta bancaria especificada no pertenece al cliente solicitante.");
        }

        if (!"ACTIVA".equalsIgnoreCase(cuenta.getEstatus())) {
            throw new OperacionNoAutorizadaException("La cuenta bancaria no se encuentra ACTIVA para realizar operaciones.");
        }

        BigDecimal saldoAnterior = cuenta.getSaldo();
        BigDecimal monto = request.getMonto();
        if (saldoAnterior.compareTo(monto) < 0) {
            throw new SaldoInsuficienteException("El saldo disponible en la cuenta es insuficiente para cubrir el monto de la operación.");
        }

        BigDecimal saldoPosterior = saldoAnterior.subtract(monto);
        cuenta.setSaldo(saldoPosterior);
        cuentaRepository.save(cuenta);

        String transaccionLocalId = "TX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        TransaccionPago tx = new TransaccionPago();
        tx.setIdempotencyKey(idempotencyKey);
        tx.setTransaccionLocalId(transaccionLocalId);
        tx.setCliente(cliente);
        tx.setCuenta(cuenta);
        tx.setNumeroCuenta(cuenta.getNumeroCuenta());
        tx.setIdServicio(request.getIdServicio().trim());
        tx.setIdProducto(request.getIdProducto().trim());
        tx.setNombreProducto(nombreProducto);
        tx.setReferenciaPago(request.getReferenciaPago().trim());
        tx.setMonto(monto);
        tx.setSaldoAnterior(saldoAnterior);
        tx.setSaldoPosterior(saldoPosterior);
        tx.setEstatus("EN_PROCESO");
        tx.setMensajeProveedor("Saldo reservado. Enviando operación al proveedor.");

        return transaccionPagoRepository.save(tx);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransaccionPago confirmarTransaccionExitosa(String transaccionLocalId, SendTxResponseDto respuestaProveedor) {
        TransaccionPago tx = transaccionPagoRepository.findByTransaccionLocalIdForUpdate(transaccionLocalId)
                .orElseThrow(() -> new TransaccionNoEncontradaException("No se encontró la transacción local para confirmar."));

        tx.setEstatus("EXITOSA");
        tx.setCodigoRespuestaProveedor(respuestaProveedor.getCodigo());
        String folio = respuestaProveedor.getNumAutorizacion() != null
                ? respuestaProveedor.getNumAutorizacion()
                : respuestaProveedor.getIdTransaccionProveedor();
        tx.setFolioProveedor(folio);
        tx.setMensajeProveedor(respuestaProveedor.getMensaje() != null
                ? respuestaProveedor.getMensaje()
                : "Pago de servicio procesado exitosamente.");

        return transaccionPagoRepository.save(tx);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransaccionPago reversarSaldoPorRechazoDefinitivo(String transaccionLocalId, SendTxResponseDto respuestaProveedor) {
        TransaccionPago tx = transaccionPagoRepository.findByTransaccionLocalIdForUpdate(transaccionLocalId)
                .orElseThrow(() -> new TransaccionNoEncontradaException("No se encontró la transacción local para aplicar reverso."));

        if ("EN_PROCESO".equals(tx.getEstatus())) {
            Cuenta cuenta = cuentaRepository.findByNumeroCuentaForUpdate(tx.getNumeroCuenta())
                    .orElseThrow(() -> new CuentaNoEncontradaException("No se encontró la cuenta para restituir el saldo."));

            BigDecimal saldoRestituido = cuenta.getSaldo().add(tx.getMonto());
            cuenta.setSaldo(saldoRestituido);
            cuentaRepository.save(cuenta);

            tx.setSaldoPosterior(saldoRestituido);
            tx.setEstatus("RECHAZADA_REVERSADA");
            tx.setCodigoRespuestaProveedor(respuestaProveedor.getCodigo());
            tx.setMensajeProveedor("Operación rechazada por el proveedor. El saldo fue restituido íntegramente a la cuenta.");
        }

        return transaccionPagoRepository.save(tx);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransaccionPago marcarPendienteConciliacion(String transaccionLocalId, SendTxResponseDto respuestaProveedor) {
        TransaccionPago tx = transaccionPagoRepository.findByTransaccionLocalIdForUpdate(transaccionLocalId)
                .orElseThrow(() -> new TransaccionNoEncontradaException("No se encontró la transacción local para marcar en conciliación."));

        tx.setEstatus("PENDIENTE_CONCILIACION");
        tx.setCodigoRespuestaProveedor(respuestaProveedor != null ? respuestaProveedor.getCodigo() : "TIMEOUT");
        tx.setMensajeProveedor("Respuesta indeterminada por tiempo de espera del proveedor. Operación retenida para conciliación segura.");

        return transaccionPagoRepository.save(tx);
    }

    @Transactional
    public TransaccionPago conciliarTransaccion(ConciliacionTransaccionRequest request) {
        TransaccionPago tx = transaccionPagoRepository.findByTransaccionLocalIdForUpdate(request.getTransaccionLocalId().trim())
                .orElseThrow(() -> new TransaccionNoEncontradaException(
                        "No se encontró la transacción con identificador: " + request.getTransaccionLocalId()));

        if (!"PENDIENTE_CONCILIACION".equals(tx.getEstatus())) {
            throw new ValidacionException("Solo las transacciones en estado PENDIENTE_CONCILIACION pueden ser conciliadas. Estado actual: " + tx.getEstatus());
        }

        if ("CONFIRMAR_EXITOSA".equals(request.getResolucionProveedor())) {
            tx.setEstatus("EXITOSA");
            tx.setFolioProveedor(request.getFolioProveedor());
            tx.setCodigoRespuestaProveedor("00");
            tx.setMensajeProveedor(request.getMotivo() != null && !request.getMotivo().isBlank()
                    ? request.getMotivo().trim()
                    : "Transacción confirmada exitosamente mediante conciliación con el proveedor.");
        } else if ("REVERSAR_SALDO".equals(request.getResolucionProveedor())) {
            Cuenta cuenta = cuentaRepository.findByNumeroCuentaForUpdate(tx.getNumeroCuenta())
                    .orElseThrow(() -> new CuentaNoEncontradaException("No se encontró la cuenta asociada para reversar el saldo."));

            BigDecimal saldoRestituido = cuenta.getSaldo().add(tx.getMonto());
            cuenta.setSaldo(saldoRestituido);
            cuentaRepository.save(cuenta);

            tx.setSaldoPosterior(saldoRestituido);
            tx.setEstatus("REVERSADA_POR_CONCILIACION");
            tx.setCodigoRespuestaProveedor("REVERSO_CONCILIACION");
            tx.setMensajeProveedor(request.getMotivo() != null && !request.getMotivo().isBlank()
                    ? request.getMotivo().trim()
                    : "El proveedor confirmó que el cargo no fue aplicado. Saldo restituido a la cuenta.");
        }

        return transaccionPagoRepository.save(tx);
    }
}

