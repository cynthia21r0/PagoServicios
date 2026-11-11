package com.proyecto.servicios;

import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.model.*;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import com.proyecto.servicios.security.BCryptPasswordEncoder;
import com.proyecto.servicios.security.JwtService;
import com.proyecto.servicios.service.Impl.AuthServiceImpl;
import com.proyecto.servicios.service.Impl.ClienteServiceImpl;
import com.proyecto.servicios.service.Impl.CuentaServiceImpl;
import com.proyecto.servicios.service.Impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Spy
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Spy
    private JwtService jwtService = new JwtService();

    @InjectMocks
    private ClienteServiceImpl clienteService;

    @InjectMocks
    private CuentaServiceImpl cuentaService;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(clienteService, "saldoInicialSistema", new BigDecimal("1000.00"));
        ReflectionTestUtils.setField(cuentaService, "saldoInicialSistema", new BigDecimal("1000.00"));
        ReflectionTestUtils.setField(jwtService, "secret", "OnboardingPersonasFisicasSecretKey2026FinancieraJwtTokenSeguridad");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86400000L);
    }

    private ClienteRegistroRequest buildClienteValido() {
        return ClienteRegistroRequest.builder()
                .nombre("Juan")
                .segundoNombre("Carlos")
                .apellidoPaterno("Perez")
                .apellidoMaterno("Gomez")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .curp("PEGJ900515HDFRNS09")
                .rfc("PEGJ900515AB1")
                .sexo("MASCULINO")
                .nacionalidad("MEXICANA")
                .estadoCivil("SOLTERO")
                .correoElectronico("juan.perez@ejemplo.com")
                .telefonoMovil("5512345678")
                .telefonoAlternativo("5587654321")
                .calle("Av Reforma")
                .numeroExterior("123")
                .numeroInterior("4B")
                .colonia("Juarez")
                .municipio("Cuauhtemoc")
                .estado("Ciudad de Mexico")
                .codigoPostal("06600")
                .pais("Mexico")
                .ocupacion("Ingeniero de Software")
                .empresa("Tech Corp")
                .ingresoMensual(new BigDecimal("35000.00"))
                .password("Segura123!")
                .build();
    }

    @Test
    void registrarCliente_Exitoso_CreaCuentaYUsuarioCifrado() {
        ClienteRegistroRequest req = buildClienteValido();

        when(clienteRepository.existsByCurpIgnoreCase(anyString())).thenReturn(false);
        when(clienteRepository.existsByRfcIgnoreCase(anyString())).thenReturn(false);
        when(clienteRepository.existsByCorreoElectronicoIgnoreCase(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCorreoIgnoreCase(anyString())).thenReturn(false);
        when(cuentaRepository.existsByNumeroCuenta(anyString())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(1L);
            c.getCuentas().get(0).setId(10L);
            c.getUsuario().setId(100L);
            return c;
        });

        ClienteResponse res = clienteService.registrarCliente(req);

        assertNotNull(res);
        assertEquals(1L, res.getId());
        assertTrue(res.getActivo());
        assertNotNull(res.getDomicilio());
        assertEquals("06600", res.getDomicilio().getCodigoPostal());
        assertEquals(1, res.getCuentas().size());
        assertEquals("ACTIVA", res.getCuentas().get(0).getEstatus());
        assertEquals(new BigDecimal("1000.00"), res.getCuentas().get(0).getSaldo());
        assertNotNull(res.getUsuario());
        assertEquals("juan.perez@ejemplo.com", res.getUsuario().getCorreo());
        assertTrue(res.getUsuario().getActivo());
    }

    @Test
    void registrarCliente_MenorDeEdad_LanzaValidacionException() {
        ClienteRegistroRequest req = buildClienteValido();
        req.setFechaNacimiento(LocalDate.now().minusYears(16));

        assertThrows(ValidacionException.class, () -> clienteService.registrarCliente(req));
    }

    @Test
    void registrarCliente_PasswordDebil_LanzaPasswordInvalidaException() {
        ClienteRegistroRequest req = buildClienteValido();
        req.setPassword("debil123");

        assertThrows(PasswordInvalidaException.class, () -> clienteService.registrarCliente(req));
    }

    @Test
    void actualizarCliente_IntentoModificarCurpORfc_LanzaValidacionException() {
        Cliente existente = new Cliente();
        existente.setId(1L);
        existente.setCurp("PEGJ900515HDFRNS09");
        existente.setRfc("PEGJ900515AB1");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));

        ClienteActualizacionRequest updateCurp = ClienteActualizacionRequest.builder()
                .curp("OTRA900515HDFRNS01")
                .build();

        assertThrows(ValidacionException.class, () -> clienteService.actualizarCliente(1L, updateCurp));
    }

    @Test
    void darBajaLogica_DesactivaClienteUsuarioYCuentas() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Juan");
        cliente.setApellidoPaterno("Perez");
        cliente.setApellidoMaterno("Gomez");
        cliente.setActivo(true);

        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setCorreo("juan.perez@ejemplo.com");
        usuario.setActivo(true);
        usuario.setCliente(cliente);
        cliente.setUsuario(usuario);

        Cuenta cuenta = new Cuenta();
        cuenta.setId(20L);
        cuenta.setNumeroCuenta("1234567890");
        cuenta.setSaldo(new BigDecimal("1000.00"));
        cuenta.setEstatus("ACTIVA");
        cuenta.setCliente(cliente);
        cliente.getCuentas().add(cuenta);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse res = clienteService.darBajaLogica(1L);

        assertFalse(res.getActivo());
        assertFalse(res.getUsuario().getActivo());
        assertEquals("INACTIVA", res.getCuentas().get(0).getEstatus());
    }

    @Test
    void login_ExitosoYValidacionTokenJwt() {
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setCorreo("juan.perez@ejemplo.com");
        usuario.setPassword(passwordEncoder.encode("Segura123!"));
        usuario.setActivo(true);

        when(usuarioRepository.findByCorreoIgnoreCase("juan.perez@ejemplo.com")).thenReturn(Optional.of(usuario));

        LoginResponse loginResponse = authService.login(new LoginRequest("juan.perez@ejemplo.com", "Segura123!"));

        assertNotNull(loginResponse.getToken());
        assertEquals("Bearer", loginResponse.getTipoToken());
        assertEquals("juan.perez@ejemplo.com", jwtService.obtenerCorreoDeToken(loginResponse.getToken()));
    }

    @Test
    void login_UsuarioInactivo_LanzaUsuarioInactivoException() {
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setCorreo("inactivo@ejemplo.com");
        usuario.setPassword(passwordEncoder.encode("Segura123!"));
        usuario.setActivo(false);

        when(usuarioRepository.findByCorreoIgnoreCase("inactivo@ejemplo.com")).thenReturn(Optional.of(usuario));

        assertThrows(UsuarioInactivoException.class,
                () -> authService.login(new LoginRequest("inactivo@ejemplo.com", "Segura123!")));
    }
}
