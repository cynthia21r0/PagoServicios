package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.exception.ClienteNoEncontradoException;
import com.proyecto.servicios.exception.CorreoDuplicadoException;
import com.proyecto.servicios.exception.UsuarioNoEncontradoException;
import com.proyecto.servicios.exception.ValidacionException;
import com.proyecto.servicios.model.UsuarioActualizacionRequest;
import com.proyecto.servicios.model.UsuarioBajaRequest;
import com.proyecto.servicios.model.UsuarioFiltroRequest;
import com.proyecto.servicios.model.UsuarioRequest;
import com.proyecto.servicios.model.UsuarioResponse;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import com.proyecto.servicios.security.BCryptPasswordEncoder;
import com.proyecto.servicios.service.UsuarioService;
import com.proyecto.servicios.util.ValidadorNegocio;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public List<UsuarioResponse> filtrarUsuarios(UsuarioFiltroRequest filtro) {
        if (filtro == null) {
            return usuarioRepository.findAll()
                    .stream()
                    .map(this::mapearAUsuarioResponse)
                    .collect(Collectors.toList());
        }

        if (filtro.getFechaInicio() != null && filtro.getFechaFin() != null
                && filtro.getFechaInicio().isAfter(filtro.getFechaFin())) {
            throw new ValidacionException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        Specification<Usuario> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getId() != null) {
                predicates.add(cb.equal(root.get("id"), filtro.getId()));
            }
            if (filtro.getClienteId() != null) {
                predicates.add(cb.equal(root.get("cliente").get("id"), filtro.getClienteId()));
            }
            if (filtro.getCorreo() != null && !filtro.getCorreo().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("correo")), "%" + filtro.getCorreo().trim().toLowerCase() + "%"));
            }
            if (filtro.getActivo() != null) {
                predicates.add(cb.equal(root.get("activo"), filtro.getActivo()));
            }
            if (filtro.getFechaInicio() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), filtro.getFechaInicio().atStartOfDay()));
            }
            if (filtro.getFechaFin() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), filtro.getFechaFin().atTime(LocalTime.MAX)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Usuario> resultados = usuarioRepository.findAll(spec);
        if (resultados.isEmpty() && (filtro.getId() != null || filtro.getClienteId() != null)) {
            throw new UsuarioNoEncontradoException("No se encontró ningún usuario que coincida con los criterios especificados.");
        }

        return resultados.stream()
                .map(this::mapearAUsuarioResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> filtrarUsuarios(Long id, Long clienteId, String correo, Boolean activo) {
        UsuarioFiltroRequest filtro = UsuarioFiltroRequest.builder()
                .id(id)
                .clienteId(clienteId)
                .correo(correo)
                .activo(activo)
                .build();
        return filtrarUsuarios(filtro);
    }

    @Override
    @Transactional
    public UsuarioResponse agregarUsuario(UsuarioRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos del usuario son obligatorios.");
        }
        if (request.getId() != null || request.getFechaCreacion() != null || request.getFechaActualizacion() != null) {
            throw new ValidacionException("No está permitido asignar manualmente campos controlados por el servidor (id, fechaCreacion, fechaActualizacion).");
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

    @Override
    @Transactional
    public UsuarioResponse actualizarUsuario(UsuarioActualizacionRequest request) {
        if (request == null || request.getId() == null) {
            throw new ValidacionException("El identificador del usuario (id) es obligatorio.");
        }
        if (request.getFechaCreacion() != null || request.getFechaActualizacion() != null || request.getClienteId() != null) {
            throw new ValidacionException("No está permitido modificar directamente campos controlados por el servidor (clienteId, fechaCreacion, fechaActualizacion).");
        }

        Usuario usuario = usuarioRepository.findById(request.getId())
                .orElseThrow(() -> new UsuarioNoEncontradoException("No se encontró el usuario con ID: " + request.getId()));

        if (request.getCorreo() != null && !request.getCorreo().isBlank()) {
            ValidadorNegocio.validarCorreo(request.getCorreo());
            String nuevoCorreo = request.getCorreo().trim().toLowerCase();
            if (!nuevoCorreo.equalsIgnoreCase(usuario.getCorreo())) {
                Optional<Usuario> existente = usuarioRepository.findByCorreoIgnoreCase(nuevoCorreo);
                if (existente.isPresent() && !existente.get().getId().equals(usuario.getId())) {
                    throw new CorreoDuplicadoException("Ya existe otro usuario registrado con el correo electrónico: " + nuevoCorreo);
                }
                usuario.setCorreo(nuevoCorreo);
                if (usuario.getCliente() != null) {
                    usuario.setCliente(usuario.getCliente());
                    usuario.getCliente().setCorreoElectronico(nuevoCorreo);
                }
            }
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            ValidadorNegocio.validarPassword(request.getPassword());
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getActivo() != null) {
            if (Boolean.TRUE.equals(request.getActivo()) && usuario.getCliente() != null && !Boolean.TRUE.equals(usuario.getCliente().getActivo())) {
                throw new ValidacionException("No se puede activar un usuario cuyo cliente asociado se encuentra dado de baja lógica.");
            }
            usuario.setActivo(request.getActivo());
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return mapearAUsuarioResponse(actualizado);
    }

    @Override
    @Transactional
    public UsuarioResponse darBajaUsuario(UsuarioBajaRequest request) {
        if (request == null || request.getId() == null) {
            throw new ValidacionException("El identificador del usuario (id) es obligatorio para realizar la baja.");
        }

        Usuario usuario = usuarioRepository.findById(request.getId())
                .orElseThrow(() -> new UsuarioNoEncontradoException("No se encontró el usuario con ID: " + request.getId()));

        usuario.setActivo(false);
        Usuario actualizado = usuarioRepository.save(usuario);
        return mapearAUsuarioResponse(actualizado);
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
