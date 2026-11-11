package com.proyecto.servicios.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.exception.ErrorResponse;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${security.jwt.enabled:true}")
    private boolean jwtEnabled;

    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!jwtEnabled) {
            return true;
        }
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        if (path.startsWith("/auth/login")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")
                || path.startsWith("/personas")
                || path.startsWith("/productos")) {
            return true;
        }
        if ("/clientes".equals(path) && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        if (path.startsWith("/usuarios/agregar") && ("PUT".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method))) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            escribirError(response, request, HttpStatus.UNAUTHORIZED, "Acceso no autorizado",
                    "Se requiere un token JWT válido en el encabezado Authorization (Bearer <token>).");
            return;
        }

        String token = authHeader.substring(7).trim();
        DecodedJWT decodedJWT = jwtService.validarToken(token);
        if (decodedJWT == null || decodedJWT.getSubject() == null) {
            escribirError(response, request, HttpStatus.UNAUTHORIZED, "Token inválido o expirado",
                    "El token JWT proporcionado no es válido o ha expirado.");
            return;
        }

        String correo = decodedJWT.getSubject();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoIgnoreCase(correo);
        if (usuarioOpt.isEmpty()) {
            escribirError(response, request, HttpStatus.UNAUTHORIZED, "Usuario no encontrado",
                    "El usuario asociado al token no existe en el sistema.");
            return;
        }

        Usuario usuario = usuarioOpt.get();
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            escribirError(response, request, HttpStatus.FORBIDDEN, "Usuario inactivo",
                    "El acceso está denegado porque el usuario se encuentra inactivo.");
            return;
        }

        request.setAttribute("usuarioAutenticado", usuario);
        filterChain.doFilter(request, response);
    }

    private void escribirError(HttpServletResponse response, HttpServletRequest request, HttpStatus status, String error, String mensaje)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .mensaje(mensaje)
                .path(request.getRequestURI())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
