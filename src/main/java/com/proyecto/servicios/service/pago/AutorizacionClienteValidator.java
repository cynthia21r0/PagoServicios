package com.proyecto.servicios.service.pago;

import com.proyecto.servicios.entity.onboarding.Cliente;
import com.proyecto.servicios.entity.onboarding.Cuenta;
import com.proyecto.servicios.entity.onboarding.Usuario;
import com.proyecto.servicios.exception.AccesoDenegadoException;
import com.proyecto.servicios.exception.UnauthorizedAccessException;
import com.proyecto.servicios.repository.onboarding.CuentaRepository;
import com.proyecto.servicios.repository.onboarding.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AutorizacionClienteValidator {

    static final String MSG_SIN_PERMISO = "No tiene permiso para operar esta cuenta";

    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final PasswordEncoder passwordEncoder;

    public AutorizacionClienteValidator(UsuarioRepository usuarioRepository,
                                        CuentaRepository cuentaRepository,
                                        PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.cuentaRepository = cuentaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Cuenta autorizar(String correoAutenticado, String numeroCuenta, String passwordOPin) {
        if (correoAutenticado == null || correoAutenticado.isBlank()) {
            throw new AccesoDenegadoException(MSG_SIN_PERMISO);
        }

        Usuario usuario = usuarioRepository.findByCorreo(correoAutenticado)
                .orElseThrow(() -> new AccesoDenegadoException(MSG_SIN_PERMISO));
        Cliente cliente = usuario.getCliente();
        if (!Boolean.TRUE.equals(usuario.getActivo()) || cliente == null
                || !Boolean.TRUE.equals(cliente.getActivo()) || Boolean.TRUE.equals(cliente.getBloqueado())) {
            throw new AccesoDenegadoException(MSG_SIN_PERMISO);
        }

        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .filter(c -> c.getCliente() != null && cliente.getId().equals(c.getCliente().getId()))
                .orElseThrow(() -> new AccesoDenegadoException(MSG_SIN_PERMISO));

        if (cuenta.getEstatus() != com.proyecto.servicios.entity.onboarding.catalogos.EstatusCuenta.ACTIVA) {
            throw new com.proyecto.servicios.exception.BusinessValidationException(
                "La cuenta se encuentra " + cuenta.getEstatus() + " y no puede realizar transacciones financieras."
            );
        }

        if (passwordOPin != null) {
            boolean coincidePassword = passwordEncoder.matches(passwordOPin, usuario.getPassword());
            boolean coincidePin = usuario.getPinTransaccional() != null && passwordEncoder.matches(passwordOPin, usuario.getPinTransaccional());
            if (!coincidePassword && !coincidePin) {
                throw new UnauthorizedAccessException("El password o PIN de autorizacion es incorrecto");
            }
        }
        return cuenta;
    }
}