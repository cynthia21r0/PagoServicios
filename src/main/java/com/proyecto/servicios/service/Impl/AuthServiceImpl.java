package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.exception.CredencialesInvalidasException;
import com.proyecto.servicios.exception.UsuarioInactivoException;
import com.proyecto.servicios.exception.UsuarioNoEncontradoException;
import com.proyecto.servicios.exception.ValidacionException;
import com.proyecto.servicios.model.LoginRequest;
import com.proyecto.servicios.model.LoginResponse;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import com.proyecto.servicios.security.BCryptPasswordEncoder;
import com.proyecto.servicios.security.JwtService;
import com.proyecto.servicios.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        if (request == null || request.getCorreo() == null || request.getCorreo().trim().isEmpty()
                || request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ValidacionException("Debe proporcionar correo electrónico y contraseña para iniciar sesión.");
        }

        String correoLimpio = request.getCorreo().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correoLimpio)
                .orElseThrow(() -> new UsuarioNoEncontradoException("No existe un usuario registrado con el correo: " + correoLimpio));

        if (!Boolean.TRUE.equals(usuario.getActivo())
                || (usuario.getCliente() != null && !Boolean.TRUE.equals(usuario.getCliente().getActivo()))) {
            throw new UsuarioInactivoException("El acceso ha sido denegado porque el usuario se encuentra inactivo.");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new CredencialesInvalidasException("Las credenciales proporcionadas son incorrectas.");
        }

        String token = jwtService.generarToken(usuario);

        return LoginResponse.builder()
                .token(token)
                .tipoToken("Bearer")
                .usuarioId(usuario.getId())
                .clienteId(usuario.getCliente() != null ? usuario.getCliente().getId() : null)
                .correo(usuario.getCorreo())
                .expiracionMs(jwtService.getExpirationMs())
                .mensaje("Inicio de sesión exitoso.")
                .build();
    }
}
