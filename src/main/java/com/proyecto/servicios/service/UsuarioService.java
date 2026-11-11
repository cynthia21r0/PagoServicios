package com.proyecto.servicios.service;

import com.proyecto.servicios.model.UsuarioActualizacionRequest;
import com.proyecto.servicios.model.UsuarioBajaRequest;
import com.proyecto.servicios.model.UsuarioFiltroRequest;
import com.proyecto.servicios.model.UsuarioRequest;
import com.proyecto.servicios.model.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponse> filtrarUsuarios(UsuarioFiltroRequest filtro);

    List<UsuarioResponse> filtrarUsuarios(Long id, Long clienteId, String correo, Boolean activo);

    UsuarioResponse agregarUsuario(UsuarioRequest request);

    UsuarioResponse actualizarUsuario(UsuarioActualizacionRequest request);

    UsuarioResponse darBajaUsuario(UsuarioBajaRequest request);
}
