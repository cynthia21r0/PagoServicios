package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.UsuarioRequest;
import com.proyecto.servicios.model.UsuarioResponse;
import com.proyecto.servicios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "API para gestión y consulta de usuarios de acceso al sistema")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Consultar y filtrar usuarios por id, clienteId, correo o estatus activo")
    @GetMapping(value = "/filtro", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UsuarioResponse>> filtrarUsuarios(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(usuarioService.filtrarUsuarios(id, clienteId, correo, activo));
    }

    @Operation(summary = "Agregar un nuevo usuario con contraseña cifrada en BCrypt (PUT)")
    @PutMapping(value = "/agregar", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioResponse> agregarUsuarioPut(@RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.agregarUsuario(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Agregar un nuevo usuario con contraseña cifrada en BCrypt (POST)")
    @PostMapping(value = "/agregar", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioResponse> agregarUsuarioPost(@RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.agregarUsuario(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

