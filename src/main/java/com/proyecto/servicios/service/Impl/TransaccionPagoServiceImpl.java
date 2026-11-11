package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.client.GestoPagoSendTxClient;
import com.proyecto.servicios.entity.mongo.CatProductMongo;
import com.proyecto.servicios.entity.sf.TransaccionPago;
import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.model.ConciliacionTransaccionRequest;
import com.proyecto.servicios.model.PagoServicioRequest;
import com.proyecto.servicios.model.PagoServicioResponse;
import com.proyecto.servicios.model.TransaccionFiltroRequest;
import com.proyecto.servicios.model.gestopago.CatProductXmlDto;
import com.proyecto.servicios.model.gestopago.ProductStatusEnum;
import com.proyecto.servicios.model.gestopago.SendTxRequestDto;
import com.proyecto.servicios.model.gestopago.SendTxResponseDto;
import com.proyecto.servicios.repositorys.mongo.CatProductMongoRepository;
import com.proyecto.servicios.repositorys.sf.TransaccionPagoRepository;
import com.proyecto.servicios.service.TransaccionPagoService;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransaccionPagoServiceImpl implements TransaccionPagoService {

    @Autowired
    private TransaccionLedgerService ledgerService;

    @Autowired
    private GestoPagoProductClient productClient;

    @Autowired
    private GestoPagoSendTxClient sendTxClient;

    @Autowired
    private TransaccionPagoRepository transaccionPagoRepository;

    @Autowired(required = false)
    private CatProductMongoRepository catProductMongoRepository;

    @Value("${gestopago.tx.monto-maximo-permitido:50000.00}")
    private BigDecimal montoMaximoPermitido;

    @Override
    public PagoServicioResponse procesarPagoServicio(PagoServicioRequest request, Usuario usuarioAutenticado) {
        if (request == null) {
            throw new ValidacionException("Los datos de la solicitud de pago son obligatorios.");
        }

        String nombreProductoValidado = validarProductoYMontoEnCatalogo(
                request.getIdServicio().trim(),
                request.getIdProducto().trim(),
                request.getMonto()
        );

        TransaccionPago transaccionReservada = ledgerService.reservarSaldoYRegistrarTransaccion(
                request,
                nombreProductoValidado,
                usuarioAutenticado
        );

        SendTxRequestDto sendTxRequest = SendTxRequestDto.builder()
                .idServicio(transaccionReservada.getIdServicio())
                .idProducto(transaccionReservada.getIdProducto())
                .referencia(transaccionReservada.getReferenciaPago())
                .telefono(request.getTelefonoDestino())
                .monto(transaccionReservada.getMonto())
                .transaccionLocalId(transaccionReservada.getTransaccionLocalId())
                .build();

        SendTxResponseDto respuestaProveedor = sendTxClient.enviarTransaccion(sendTxRequest);

        if (respuestaProveedor.isAprobada()) {
            TransaccionPago confirmada = ledgerService.confirmarTransaccionExitosa(
                    transaccionReservada.getTransaccionLocalId(),
                    respuestaProveedor
            );
            return mapearAResponse(confirmada);
        }

        if (respuestaProveedor.isEstadoIndeterminado()) {
            TransaccionPago enConciliacion = ledgerService.marcarPendienteConciliacion(
                    transaccionReservada.getTransaccionLocalId(),
                    respuestaProveedor
            );
            throw new TransaccionPendienteConciliacionException(
                    "El proveedor no respondió dentro del tiempo límite. Por seguridad financiera no se reintentó el cargo automáticamente y la operación quedó en estado PENDIENTE_CONCILIACION con folio local: "
                            + enConciliacion.getTransaccionLocalId(),
                    enConciliacion.getTransaccionLocalId()
            );
        }

        TransaccionPago reversada = ledgerService.reversarSaldoPorRechazoDefinitivo(
                transaccionReservada.getTransaccionLocalId(),
                respuestaProveedor
        );
        throw new ProveedorRechazoTransaccionException(
                "La operación fue rechazada por el proveedor y el saldo de su cuenta ha sido restituido íntegramente. Folio local: "
                        + reversada.getTransaccionLocalId()
        );
    }

    @Override
    public PagoServicioResponse conciliarTransaccion(ConciliacionTransaccionRequest request) {
        TransaccionPago conciliada = ledgerService.conciliarTransaccion(request);
        return mapearAResponse(conciliada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoServicioResponse> filtrarTransacciones(TransaccionFiltroRequest filtro) {
        if (filtro == null) {
            return transaccionPagoRepository.findAll()
                    .stream()
                    .map(this::mapearAResponse)
                    .collect(Collectors.toList());
        }

        if (filtro.getFechaInicio() != null && filtro.getFechaFin() != null
                && filtro.getFechaInicio().isAfter(filtro.getFechaFin())) {
            throw new ValidacionException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        Specification<TransaccionPago> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getClienteId() != null) {
                predicates.add(cb.equal(root.get("cliente").get("id"), filtro.getClienteId()));
            }
            if (filtro.getNumeroCuenta() != null && !filtro.getNumeroCuenta().isBlank()) {
                predicates.add(cb.equal(root.get("numeroCuenta"), filtro.getNumeroCuenta().trim()));
            }
            if (filtro.getEstatus() != null && !filtro.getEstatus().isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("estatus")), filtro.getEstatus().trim().toUpperCase()));
            }
            if (filtro.getIdServicio() != null && !filtro.getIdServicio().isBlank()) {
                predicates.add(cb.equal(root.get("idServicio"), filtro.getIdServicio().trim()));
            }
            if (filtro.getIdProducto() != null && !filtro.getIdProducto().isBlank()) {
                predicates.add(cb.equal(root.get("idProducto"), filtro.getIdProducto().trim()));
            }
            if (filtro.getIdempotencyKey() != null && !filtro.getIdempotencyKey().isBlank()) {
                predicates.add(cb.equal(root.get("idempotencyKey"), filtro.getIdempotencyKey().trim()));
            }
            if (filtro.getTransaccionLocalId() != null && !filtro.getTransaccionLocalId().isBlank()) {
                predicates.add(cb.equal(root.get("transaccionLocalId"), filtro.getTransaccionLocalId().trim()));
            }
            if (filtro.getFechaInicio() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), filtro.getFechaInicio().atStartOfDay()));
            }
            if (filtro.getFechaFin() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), filtro.getFechaFin().atTime(LocalTime.MAX)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return transaccionPagoRepository.findAll(spec)
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    private String validarProductoYMontoEnCatalogo(String idServicio, String idProducto, BigDecimal montoSolicitado) {
        if (montoSolicitado == null || montoSolicitado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MontoInvalidoException("El monto de la operación debe ser mayor a cero.");
        }
        if (montoSolicitado.compareTo(montoMaximoPermitido) > 0) {
            throw new MontoInvalidoException("El monto solicitado excede el límite máximo permitido por operación (" + montoMaximoPermitido + ").");
        }

        CatProductXmlDto catalogoXml = null;
        try {
            catalogoXml = productClient.getProductListXml();
        } catch (Exception e) {
            log.warn("No se pudo obtener catálogo en línea de GestoPago, verificando respaldo local.");
        }

        if (catalogoXml != null && catalogoXml.getProducts() != null && !catalogoXml.getProducts().isEmpty()) {
            CatProductXmlDto.ProductXmlItem itemEncontrado = catalogoXml.getProducts().stream()
                    .filter(item -> coincideServicioYProducto(item, idServicio, idProducto))
                    .findFirst()
                    .orElseThrow(() -> new ProductoInvalidoException(
                            "El servicio (" + idServicio + ") y producto (" + idProducto + ") solicitados no existen en el catálogo autorizado."));

            validarPrecioProducto(itemEncontrado.getPrecio(), montoSolicitado);
            return itemEncontrado.getNombreProducto() != null ? itemEncontrado.getNombreProducto() : itemEncontrado.getServicio();
        }

        if (catProductMongoRepository != null) {
            Optional<CatProductMongo> productoMongo = catProductMongoRepository.findAll().stream()
                    .filter(p -> idProducto.equals(p.getProductId()))
                    .findFirst();

            if (productoMongo.isPresent()) {
                if (productoMongo.get().getStatus() != ProductStatusEnum.ACTIVE) {
                    throw new ProductoInvalidoException("El producto solicitado no se encuentra activo en el catálogo.");
                }
                return productoMongo.get().getName();
            }
        }

        throw new ProductoInvalidoException("No fue posible verificar la existencia y vigencia del producto en el catálogo autorizado.");
    }

    private boolean coincideServicioYProducto(CatProductXmlDto.ProductXmlItem item, String idServicio, String idProducto) {
        boolean coincideProd = idProducto.equals(item.getIdProducto());
        boolean coincideServ = item.getIdServicio() == null || idServicio.equals(item.getIdServicio());
        return coincideProd && coincideServ;
    }

    private void validarPrecioProducto(String precioTexto, BigDecimal montoSolicitado) {
        if (precioTexto == null || precioTexto.isBlank()) {
            return;
        }
        try {
            BigDecimal precioCatalogo = new BigDecimal(precioTexto.trim());
            if (precioCatalogo.compareTo(BigDecimal.ZERO) > 0
                    && montoSolicitado.compareTo(precioCatalogo) != 0) {
                throw new MontoInvalidoException(
                        "El monto solicitado (" + montoSolicitado + ") no corresponde con el monto fijo autorizado del producto en el catálogo (" + precioCatalogo + ").");
            }
        } catch (NumberFormatException e) {
            throw new MontoInvalidoException("El precio configurado en el catálogo para el producto no es numérico válido.");
        }
    }

    private PagoServicioResponse mapearAResponse(TransaccionPago tx) {
        return PagoServicioResponse.builder()
                .transaccionId(tx.getId())
                .idempotencyKey(tx.getIdempotencyKey())
                .transaccionLocalId(tx.getTransaccionLocalId())
                .clienteId(tx.getCliente() != null ? tx.getCliente().getId() : null)
                .numeroCuenta(tx.getNumeroCuenta())
                .idServicio(tx.getIdServicio())
                .idProducto(tx.getIdProducto())
                .nombreProducto(tx.getNombreProducto())
                .referenciaPago(tx.getReferenciaPago())
                .monto(tx.getMonto())
                .saldoAnterior(tx.getSaldoAnterior())
                .saldoDisponible(tx.getSaldoPosterior())
                .estatus(tx.getEstatus())
                .folioProveedor(tx.getFolioProveedor())
                .codigoRespuestaProveedor(tx.getCodigoRespuestaProveedor())
                .mensajeOperacion(tx.getMensajeProveedor())
                .fechaCreacion(tx.getFechaCreacion())
                .fechaActualizacion(tx.getFechaActualizacion())
                .build();
    }
}

