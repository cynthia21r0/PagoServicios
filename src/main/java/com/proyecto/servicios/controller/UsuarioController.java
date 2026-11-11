package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.ApiResponse;
import com.proyecto.servicios.model.UsuarioActualizacionRequest;
import com.proyecto.servicios.model.UsuarioBajaRequest;
import com.proyecto.servicios.model.UsuarioFiltroRequest;
import com.proyecto.servicios.model.UsuarioRequest;
import com.proyecto.servicios.model.UsuarioResponse;
import com.proyecto.servicios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "API para administración, consulta dinámica y actualización de usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Consultar usuarios combinando filtros dinámicos en el cuerpo de la petición (GET /usuarios/filtro)")
    @GetMapping(value = "/filtro", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> filtrarUsuariosGet(
            @Valid @RequestBody(required = false) UsuarioFiltroRequest request) {
        List<UsuarioResponse> usuarios = usuarioService.filtrarUsuarios(request);
        return ResponseEntity.ok(ApiResponse.ok("Consulta de usuarios realizada exitosamente.", usuarios));
    }

    @Operation(summary = "Consultar usuarios combinando filtros dinámicos en el cuerpo de la petición (POST /usuarios/filtro)")
    @PostMapping(value = "/filtro", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> filtrarUsuariosPost(
            @Valid @RequestBody(required = false) UsuarioFiltroRequest request) {
        List<UsuarioResponse> usuarios = usuarioService.filtrarUsuarios(request);
        return ResponseEntity.ok(ApiResponse.ok("Consulta de usuarios realizada exitosamente.", usuarios));
    }

    @Operation(summary = "Agregar un nuevo usuario con contraseña cifrada en BCrypt (PUT /usuarios/agregar)")
    @PutMapping(value = "/agregar", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UsuarioResponse>> agregarUsuarioPut(
            @Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.agregarUsuario(request);
        return new ResponseEntity<>(ApiResponse.created("Usuario registrado exitosamente.", response), HttpStatus.CREATED);
    }

    @Operation(summary = "Agregar un nuevo usuario con contraseña cifrada en BCrypt (POST /usuarios/agregar)")
    @PostMapping(value = "/agregar", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UsuarioResponse>> agregarUsuarioPost(
            @Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.agregarUsuario(request);
        return new ResponseEntity<>(ApiResponse.created("Usuario registrado exitosamente.", response), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar parcialmente la información de un usuario")
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarUsuario(
            @Valid @RequestBody UsuarioActualizacionRequest request) {
        UsuarioResponse response = usuarioService.actualizarUsuario(request);
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado exitosamente.", response));
    }

    @Operation(summary = "Baja lógica de un usuario")
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UsuarioResponse>> darBajaUsuario(
            @Valid @RequestBody UsuarioBajaRequest request) {
        UsuarioResponse response = usuarioService.darBajaUsuario(request);
        return ResponseEntity.ok(ApiResponse.ok("Usuario desactivado exitosamente.", response));
    }
}
