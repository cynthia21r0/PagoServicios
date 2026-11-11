package com.proyecto.servicios.service;

import com.proyecto.servicios.model.UsuarioRequest;
import com.proyecto.servicios.model.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponse> filtrarUsuarios(Long id, Long clienteId, String correo, Boolean activo);

    UsuarioResponse agregarUsuario(UsuarioRequest request);
}

