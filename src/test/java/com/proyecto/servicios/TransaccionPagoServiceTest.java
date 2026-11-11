package com.proyecto.servicios;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.client.GestoPagoSendTxClient;
import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.entity.sf.TransaccionPago;
import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.model.ConciliacionTransaccionRequest;
import com.proyecto.servicios.model.PagoServicioRequest;
import com.proyecto.servicios.model.PagoServicioResponse;
import com.proyecto.servicios.model.gestopago.CatProductXmlDto;
import com.proyecto.servicios.model.gestopago.SendTxRequestDto;
import com.proyecto.servicios.model.gestopago.SendTxResponseDto;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.repositorys.sf.TransaccionPagoRepository;
import com.proyecto.servicios.service.Impl.TransaccionLedgerService;
import com.proyecto.servicios.service.Impl.TransaccionPagoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransaccionPagoServiceTest {

    @Mock
    private TransaccionPagoRepository transaccionPagoRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private GestoPagoProductClient productClient;

    @Mock
    private GestoPagoSendTxClient sendTxClient;

    @InjectMocks
    private TransaccionLedgerService ledgerService;

    @InjectMocks
    private TransaccionPagoServiceImpl transaccionPagoService;

    private Cliente clienteActivo;
    private Cuenta cuentaActiva;
    private Usuario usuarioAutenticado;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transaccionPagoService, "ledgerService", ledgerService);
        ReflectionTestUtils.setField(transaccionPagoService, "montoMaximoPermitido", new BigDecimal("50000.00"));

        clienteActivo = new Cliente();
        clienteActivo.setId(1L);
        clienteActivo.setNombre("Juan");
        clienteActivo.setApellidoPaterno("Perez");
        clienteActivo.setApellidoMaterno("Gomez");
        clienteActivo.setActivo(true);

        cuentaActiva = new Cuenta();
        cuentaActiva.setId(10L);
        cuentaActiva.setNumeroCuenta("1234567890");
        cuentaActiva.setSaldo(new BigDecimal("1000.00"));
        cuentaActiva.setEstatus("ACTIVA");
        cuentaActiva.setCliente(clienteActivo);

        usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(100L);
        usuarioAutenticado.setCorreo("juan.perez@ejemplo.com");
        usuarioAutenticado.setActivo(true);
        usuarioAutenticado.setCliente(clienteActivo);
    }

    private CatProductXmlDto buildCatalogoConProducto(String idServicio, String idProducto, String precio) {
        CatProductXmlDto.ProductXmlItem item = new CatProductXmlDto.ProductXmlItem();
        item.setIdServicio(idServicio);
        item.setIdProducto(idProducto);
        item.setNombreProducto("Recarga Telcel 100");
        item.setServicio("Telcel");
        item.setPrecio(precio);

        CatProductXmlDto dto = new CatProductXmlDto();
        dto.setProducts(List.of(item));
        return dto;
    }

    private PagoServicioRequest buildPagoRequestValido() {
        return PagoServicioRequest.builder()
                .idempotencyKey("IDEMP-2026-0001")
                .clienteId(1L)
                .numeroCuenta("1234567890")
                .idServicio("1")
                .idProducto("10")
                .referenciaPago("5512345678")
                .telefonoDestino("5512345678")
                .monto(new BigDecimal("100.00"))
                .build();
    }

    @Test
    void procesarPagoServicio_Exitoso_DescuentaSaldoYConfirmaTransaccion() {
        PagoServicioRequest req = buildPagoRequestValido();

        when(productClient.getProductListXml()).thenReturn(buildCatalogoConProducto("1", "10", "100.00"));
        when(transaccionPagoRepository.existsByIdempotencyKey("IDEMP-2026-0001")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        when(cuentaRepository.findByNumeroCuentaForUpdate("1234567890")).thenReturn(Optional.of(cuentaActiva));
        when(transaccionPagoRepository.save(any(TransaccionPago.class))).thenAnswer(inv -> {
            TransaccionPago tx = inv.getArgument(0);
            if (tx.getId() == null) {
                tx.setId(500L);
            }
            return tx;
        });

        SendTxResponseDto respuestaExito = SendTxResponseDto.builder()
                .codigo("00")
                .mensaje("Transacción exitosa")
                .numAutorizacion("AUT-987654")
                .aprobada(true)
                .rechazoDefinitivo(false)
                .estadoIndeterminado(false)
                .build();

        when(sendTxClient.enviarTransaccion(any(SendTxRequestDto.class))).thenReturn(respuestaExito);
        when(transaccionPagoRepository.findByTransaccionLocalIdForUpdate(anyString())).thenAnswer(inv -> {
            TransaccionPago tx = new TransaccionPago();
            tx.setId(500L);
            tx.setIdempotencyKey("IDEMP-2026-0001");
            tx.setTransaccionLocalId(inv.getArgument(0));
            tx.setCliente(clienteActivo);
            tx.setCuenta(cuentaActiva);
            tx.setNumeroCuenta("1234567890");
            tx.setIdServicio("1");
            tx.setIdProducto("10");
            tx.setNombreProducto("Recarga Telcel 100");
            tx.setReferenciaPago("5512345678");
            tx.setMonto(new BigDecimal("100.00"));
            tx.setSaldoAnterior(new BigDecimal("1000.00"));
            tx.setSaldoPosterior(new BigDecimal("900.00"));
            tx.setEstatus("EN_PROCESO");
            return Optional.of(tx);
        });

        PagoServicioResponse response = transaccionPagoService.procesarPagoServicio(req, usuarioAutenticado);

        assertNotNull(response);
        assertEquals("EXITOSA", response.getEstatus());
        assertEquals("AUT-987654", response.getFolioProveedor());
        assertEquals(new BigDecimal("1000.00"), response.getSaldoAnterior());
        assertEquals(new BigDecimal("900.00"), response.getSaldoDisponible());
        assertEquals(new BigDecimal("900.00"), cuentaActiva.getSaldo());
    }

    @Test
    void procesarPagoServicio_RechazoDefinitivoProveedor_ReversaSaldoAutomaticamente() {
        PagoServicioRequest req = buildPagoRequestValido();

        when(productClient.getProductListXml()).thenReturn(buildCatalogoConProducto("1", "10", "100.00"));
        when(transaccionPagoRepository.existsByIdempotencyKey("IDEMP-2026-0001")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        when(cuentaRepository.findByNumeroCuentaForUpdate("1234567890")).thenReturn(Optional.of(cuentaActiva));
        when(transaccionPagoRepository.save(any(TransaccionPago.class))).thenAnswer(inv -> inv.getArgument(0));

        SendTxResponseDto respuestaRechazo = SendTxResponseDto.builder()
                .codigo("14")
                .mensaje("Referencia inválida")
                .aprobada(false)
                .rechazoDefinitivo(true)
                .estadoIndeterminado(false)
                .build();

        when(sendTxClient.enviarTransaccion(any(SendTxRequestDto.class))).thenReturn(respuestaRechazo);
        when(transaccionPagoRepository.findByTransaccionLocalIdForUpdate(anyString())).thenAnswer(inv -> {
            TransaccionPago tx = new TransaccionPago();
            tx.setTransaccionLocalId(inv.getArgument(0));
            tx.setNumeroCuenta("1234567890");
            tx.setMonto(new BigDecimal("100.00"));
            tx.setSaldoAnterior(new BigDecimal("1000.00"));
            tx.setSaldoPosterior(new BigDecimal("900.00"));
            tx.setEstatus("EN_PROCESO");
            return Optional.of(tx);
        });

        assertThrows(ProveedorRechazoTransaccionException.class,
                () -> transaccionPagoService.procesarPagoServicio(req, usuarioAutenticado));

        assertEquals(new BigDecimal("1000.00"), cuentaActiva.getSaldo());
    }

    @Test
    void procesarPagoServicio_TimeoutProveedor_MarcaPendienteConciliacionYPermiteConciliar() {
        PagoServicioRequest req = buildPagoRequestValido();

        when(productClient.getProductListXml()).thenReturn(buildCatalogoConProducto("1", "10", "100.00"));
        when(transaccionPagoRepository.existsByIdempotencyKey("IDEMP-2026-0001")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        when(cuentaRepository.findByNumeroCuentaForUpdate("1234567890")).thenReturn(Optional.of(cuentaActiva));
        when(transaccionPagoRepository.save(any(TransaccionPago.class))).thenAnswer(inv -> inv.getArgument(0));

        SendTxResponseDto respuestaTimeout = SendTxResponseDto.builder()
                .codigo("TIMEOUT")
                .mensaje("Timeout")
                .aprobada(false)
                .rechazoDefinitivo(false)
                .estadoIndeterminado(true)
                .build();

        when(sendTxClient.enviarTransaccion(any(SendTxRequestDto.class))).thenReturn(respuestaTimeout);

        TransaccionPago txEnBd = new TransaccionPago();
        txEnBd.setId(700L);
        txEnBd.setIdempotencyKey("IDEMP-2026-0001");
        txEnBd.setCliente(clienteActivo);
        txEnBd.setCuenta(cuentaActiva);
        txEnBd.setNumeroCuenta("1234567890");
        txEnBd.setMonto(new BigDecimal("100.00"));
        txEnBd.setSaldoAnterior(new BigDecimal("1000.00"));
        txEnBd.setSaldoPosterior(new BigDecimal("900.00"));
        txEnBd.setEstatus("EN_PROCESO");

        when(transaccionPagoRepository.findByTransaccionLocalIdForUpdate(anyString())).thenAnswer(inv -> {
            txEnBd.setTransaccionLocalId(inv.getArgument(0));
            return Optional.of(txEnBd);
        });

        TransaccionPendienteConciliacionException ex = assertThrows(
                TransaccionPendienteConciliacionException.class,
                () -> transaccionPagoService.procesarPagoServicio(req, usuarioAutenticado)
        );

        assertNotNull(ex.getTransaccionLocalId());
        assertEquals("PENDIENTE_CONCILIACION", txEnBd.getEstatus());
        assertEquals(new BigDecimal("900.00"), cuentaActiva.getSaldo());

        ConciliacionTransaccionRequest conciliacionReq = ConciliacionTransaccionRequest.builder()
                .transaccionLocalId(ex.getTransaccionLocalId())
                .resolucionProveedor("REVERSAR_SALDO")
                .motivo("Proveedor confirmó que la transacción no ingresó")
                .build();

        PagoServicioResponse conciliada = transaccionPagoService.conciliarTransaccion(conciliacionReq);
        assertEquals("REVERSADA_POR_CONCILIACION", conciliada.getEstatus());
        assertEquals(new BigDecimal("1000.00"), cuentaActiva.getSaldo());
    }

    @Test
    void procesarPagoServicio_MontoManipuladoDistintoAlCatalogo_LanzaMontoInvalidoException() {
        PagoServicioRequest req = buildPagoRequestValido();
        req.setMonto(new BigDecimal("50.00"));

        when(productClient.getProductListXml()).thenReturn(buildCatalogoConProducto("1", "10", "100.00"));

        assertThrows(MontoInvalidoException.class,
                () -> transaccionPagoService.procesarPagoServicio(req, usuarioAutenticado));
    }

    @Test
    void procesarPagoServicio_SolicitudDuplicada_LanzaTransaccionDuplicadaException() {
        PagoServicioRequest req = buildPagoRequestValido();

        when(productClient.getProductListXml()).thenReturn(buildCatalogoConProducto("1", "10", "100.00"));
        when(transaccionPagoRepository.existsByIdempotencyKey("IDEMP-2026-0001")).thenReturn(true);

        assertThrows(TransaccionDuplicadaException.class,
                () -> transaccionPagoService.procesarPagoServicio(req, usuarioAutenticado));
    }

    @Test
    void procesarPagoServicio_CuentaDeOtroCliente_LanzaOperacionNoAutorizadaException() {
        PagoServicioRequest req = buildPagoRequestValido();

        Cliente otroCliente = new Cliente();
        otroCliente.setId(99L);
        otroCliente.setActivo(true);
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(999L);
        otroUsuario.setActivo(true);
        otroUsuario.setCliente(otroCliente);

        when(productClient.getProductListXml()).thenReturn(buildCatalogoConProducto("1", "10", "100.00"));
        when(transaccionPagoRepository.existsByIdempotencyKey("IDEMP-2026-0001")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));

        assertThrows(OperacionNoAutorizadaException.class,
                () -> transaccionPagoService.procesarPagoServicio(req, otroUsuario));
    }
}

