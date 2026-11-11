package com.proyecto.servicios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.proyecto.servicios.controller.AuthController;
import com.proyecto.servicios.controller.ClienteController;
import com.proyecto.servicios.controller.CuentaController;
import com.proyecto.servicios.controller.TransaccionPagoController;
import com.proyecto.servicios.controller.UsuarioController;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.exception.TransaccionPendienteConciliacionException;
import com.proyecto.servicios.exception.UsuarioNoEncontradoException;
import com.proyecto.servicios.model.*;
import com.proyecto.servicios.service.AuthService;
import com.proyecto.servicios.service.ClienteService;
import com.proyecto.servicios.service.CuentaService;
import com.proyecto.servicios.service.TransaccionPagoService;
import com.proyecto.servicios.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OnboardingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private CuentaService cuentaService;

    @Mock
    private AuthService authService;

    @Mock
    private TransaccionPagoService transaccionPagoService;

    @InjectMocks
    private UsuarioController usuarioController;

    @InjectMocks
    private ClienteController clienteController;

    @InjectMocks
    private CuentaController cuentaController;

    @InjectMocks
    private AuthController authController;

    @InjectMocks
    private TransaccionPagoController transaccionPagoController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(
                        usuarioController,
                        clienteController,
                        cuentaController,
                        authController,
                        transaccionPagoController
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void agregarUsuario_ConDatosValidos_Retorna201YApiResponse() throws Exception {
        UsuarioRequest req = UsuarioRequest.builder()
                .correo("admin@ejemplo.com")
                .password("Segura123!")
                .activo(true)
                .build();

        UsuarioResponse resp = UsuarioResponse.builder()
                .id(1L)
                .correo("admin@ejemplo.com")
                .activo(true)
                .build();

        when(usuarioService.agregarUsuario(any(UsuarioRequest.class))).thenReturn(resp);

        mockMvc.perform(put("/usuarios/agregar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exito").value(true))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.datos.correo").value("admin@ejemplo.com"));
    }

    @Test
    void agregarUsuario_ConCorreoInvalido_BeanValidationRetorna400ConDetalles() throws Exception {
        UsuarioRequest req = UsuarioRequest.builder()
                .correo("correo-invalido")
                .password("123")
                .build();

        mockMvc.perform(put("/usuarios/agregar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exito").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detallesValidacion").exists());
    }

    @Test
    void filtrarUsuarios_ConBodyJson_Retorna200YListaUniforme() throws Exception {
        UsuarioFiltroRequest filtro = UsuarioFiltroRequest.builder()
                .correo("admin")
                .activo(true)
                .build();

        UsuarioResponse u = UsuarioResponse.builder()
                .id(1L)
                .correo("admin@ejemplo.com")
                .activo(true)
                .build();

        when(usuarioService.filtrarUsuarios(any(UsuarioFiltroRequest.class))).thenReturn(List.of(u));

        mockMvc.perform(post("/usuarios/filtro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filtro)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exito").value(true))
                .andExpect(jsonPath("$.datos[0].correo").value("admin@ejemplo.com"));
    }

    @Test
    void actualizarUsuario_NoExistente_GlobalExceptionHandlerRetorna404() throws Exception {
        UsuarioActualizacionRequest req = UsuarioActualizacionRequest.builder()
                .id(999L)
                .correo("nuevo@ejemplo.com")
                .build();

        when(usuarioService.actualizarUsuario(any(UsuarioActualizacionRequest.class)))
                .thenThrow(new UsuarioNoEncontradoException("No se encontró el usuario con ID: 999"));

        mockMvc.perform(patch("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exito").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    void actualizarCuenta_ConBodyJson_Retorna200() throws Exception {
        CuentaActualizacionRequest req = CuentaActualizacionRequest.builder()
                .numeroCuenta("1234567890")
                .saldo(new BigDecimal("2500.00"))
                .estatus("ACTIVA")
                .build();

        CuentaResponse resp = CuentaResponse.builder()
                .id(10L)
                .numeroCuenta("1234567890")
                .saldo(new BigDecimal("2500.00"))
                .estatus("ACTIVA")
                .build();

        when(cuentaService.actualizarCuenta(any(CuentaActualizacionRequest.class))).thenReturn(resp);

        mockMvc.perform(patch("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exito").value(true))
                .andExpect(jsonPath("$.datos.numeroCuenta").value("1234567890"));
    }

    @Test
    void pagarServicio_Exitoso_Retorna200ConFolioProveedor() throws Exception {
        PagoServicioRequest req = PagoServicioRequest.builder()
                .idempotencyKey("IDEMP-2026-9999")
                .clienteId(1L)
                .numeroCuenta("1234567890")
                .idServicio("1")
                .idProducto("10")
                .referenciaPago("5512345678")
                .monto(new BigDecimal("100.00"))
                .build();

        PagoServicioResponse resp = PagoServicioResponse.builder()
                .transaccionId(1L)
                .idempotencyKey("IDEMP-2026-9999")
                .transaccionLocalId("TX-1234567890ABCDEF")
                .estatus("EXITOSA")
                .folioProveedor("AUT-555555")
                .monto(new BigDecimal("100.00"))
                .saldoAnterior(new BigDecimal("1000.00"))
                .saldoDisponible(new BigDecimal("900.00"))
                .build();

        when(transaccionPagoService.procesarPagoServicio(any(), any())).thenReturn(resp);

        mockMvc.perform(post("/transacciones/pagar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exito").value(true))
                .andExpect(jsonPath("$.datos.estatus").value("EXITOSA"))
                .andExpect(jsonPath("$.datos.folioProveedor").value("AUT-555555"));
    }

    @Test
    void pagarServicio_TimeoutProveedor_Retorna504ConFolioEnDetalles() throws Exception {
        PagoServicioRequest req = PagoServicioRequest.builder()
                .idempotencyKey("IDEMP-2026-TIMEOUT")
                .clienteId(1L)
                .numeroCuenta("1234567890")
                .idServicio("1")
                .idProducto("10")
                .referenciaPago("5512345678")
                .monto(new BigDecimal("100.00"))
                .build();

        when(transaccionPagoService.procesarPagoServicio(any(), any()))
                .thenThrow(new TransaccionPendienteConciliacionException("Operación retenida para conciliación", "TX-TIMEOUT001"));

        mockMvc.perform(post("/transacciones/pagar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.exito").value(false))
                .andExpect(jsonPath("$.status").value(504))
                .andExpect(jsonPath("$.detallesValidacion.transaccionLocalId").value("TX-TIMEOUT001"))
                .andExpect(jsonPath("$.detallesValidacion.estatus").value("PENDIENTE_CONCILIACION"));
    }
}
