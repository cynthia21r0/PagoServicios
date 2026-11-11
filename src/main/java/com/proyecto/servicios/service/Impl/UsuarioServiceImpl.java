package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.exception.ClienteNoEncontradoException;
import com.proyecto.servicios.exception.CorreoDuplicadoException;
import com.proyecto.servicios.exception.UsuarioNoEncontradoException;
import com.proyecto.servicios.exception.ValidacionException;
import com.proyecto.servicios.model.UsuarioRequest;
import com.proyecto.servicios.model.UsuarioResponse;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import com.proyecto.servicios.security.BCryptPasswordEncoder;
import com.proyecto.servicios.service.UsuarioService;
import com.proyecto.servicios.util.ValidadorNegocio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> filtrarUsuarios(Long id, Long clienteId, String correo, Boolean activo) {
        if (id != null) {
            Usuario u = usuarioRepository.findById(id)
                    .orElseThrow(() -> new UsuarioNoEncontradoException("No se encontró el usuario con ID: " + id));
            return Collections.singletonList(mapearAUsuarioResponse(u));
        }

        if (clienteId != null) {
            Usuario u = usuarioRepository.findByClienteId(clienteId)
                    .orElseThrow(() -> new UsuarioNoEncontradoException("No se encontró usuario asociado al cliente con ID: " + clienteId));
            return Collections.singletonList(mapearAUsuarioResponse(u));
        }

        List<Usuario> lista = usuarioRepository.findAll();

        if (correo != null && !correo.trim().isEmpty()) {
            String filtroCorreo = correo.trim().toLowerCase();
            lista = lista.stream()
                    .filter(u -> u.getCorreo() != null && u.getCorreo().toLowerCase().contains(filtroCorreo))
                    .collect(Collectors.toList());
        }

        if (activo != null) {
            lista = lista.stream()
                    .filter(u -> activo.equals(u.getActivo()))
                    .collect(Collectors.toList());
        }

        return lista.stream()
                .map(this::mapearAUsuarioResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UsuarioResponse agregarUsuario(UsuarioRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos del usuario son obligatorios.");
        }

        ValidadorNegocio.validarCorreo(request.getCorreo());
        ValidadorNegocio.validarPassword(request.getPassword());

        String correoLimpio = request.getCorreo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correoLimpio)) {
            throw new CorreoDuplicadoException("Ya existe un usuario registrado con el correo electrónico: " + correoLimpio);
        }

        Cliente cliente = null;
        if (request.getClienteId() != null) {
            cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new ClienteNoEncontradoException("No se encontró el cliente con ID: " + request.getClienteId()));

            if (usuarioRepository.existsByClienteId(cliente.getId())) {
                throw new ValidacionException("El cliente con ID " + cliente.getId() + " ya cuenta con un usuario asociado.");
            }
            if (!Boolean.TRUE.equals(cliente.getActivo())) {
                throw new ValidacionException("No se puede crear un usuario activo para un cliente dado de baja lógica.");
            }
        }

        Usuario usuario = new Usuario();
        usuario.setCliente(cliente);
        usuario.setCorreo(correoLimpio);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setActivo(request.getActivo() != null ? request.getActivo() : true);

        Usuario guardado = usuarioRepository.save(usuario);
        return mapearAUsuarioResponse(guardado);
    }

    private UsuarioResponse mapearAUsuarioResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .clienteId(usuario.getCliente() != null ? usuario.getCliente().getId() : null)
                .correo(usuario.getCorreo())
                .activo(usuario.getActivo())
                .fechaCreacion(usuario.getFechaCreacion())
                .fechaActualizacion(usuario.getFechaActualizacion())
                .build();
    }
}

