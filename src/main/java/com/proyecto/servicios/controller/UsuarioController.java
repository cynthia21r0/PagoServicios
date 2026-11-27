package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.response.GenericResponse;
import com.proyecto.servicios.model.response.PaginaResponseDTO;
import com.proyecto.servicios.model.usuario.*;
import com.proyecto.servicios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<GenericResponse<UsuarioResponseDTO>> crearUsuario(
            @Valid @RequestBody UsuarioCreacionRequest request) {
        UsuarioResponseDTO usuario = usuarioService.crearUsuario(request);
        GenericResponse<UsuarioResponseDTO> response = GenericResponse.<UsuarioResponseDTO>builder()
                .mensaje("Usuario creado exitosamente")
                .data(usuario)
                .status(HttpStatus.CREATED.value())
                .codigo(HttpStatus.CREATED.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(value = {"/buscar", "/consultar"})
    public ResponseEntity<GenericResponse<PaginaResponseDTO<UsuarioResponseDTO>>> buscarUsuarios(
            @Valid @RequestBody(required = false) UsuarioBusquedaRequest request) {
        Page<UsuarioResponseDTO> resultado = usuarioService.buscarUsuarios(request);
        GenericResponse<PaginaResponseDTO<UsuarioResponseDTO>> response = GenericResponse.<PaginaResponseDTO<UsuarioResponseDTO>>builder()
                .mensaje("Búsqueda de usuarios realizada exitosamente")
                .data(PaginaResponseDTO.desde(resultado))
                .status(HttpStatus.OK.value())
                .codigo(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<GenericResponse<UsuarioResponseDTO>> actualizarUsuario(
            @Valid @RequestBody UsuarioActualizaRequest request) {
        UsuarioResponseDTO usuario = usuarioService.actualizarUsuario(request);
        GenericResponse<UsuarioResponseDTO> response = GenericResponse.<UsuarioResponseDTO>builder()
                .mensaje("Usuario actualizado exitosamente")
                .data(usuario)
                .status(HttpStatus.OK.value())
                .codigo(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/estado")
    public ResponseEntity<GenericResponse<UsuarioResponseDTO>> cambiarEstado(
            @Valid @RequestBody UsuarioCambioEstadoRequest request) {
        UsuarioResponseDTO usuario = usuarioService.cambiarEstado(request);
        GenericResponse<UsuarioResponseDTO> response = GenericResponse.<UsuarioResponseDTO>builder()
                .mensaje("Estado del usuario actualizado exitosamente")
                .data(usuario)
                .status(HttpStatus.OK.value())
                .codigo(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<GenericResponse<Void>> eliminarUsuario(
            @Valid @RequestBody UsuarioEliminaRequest request) {
        usuarioService.eliminarUsuario(request);
        GenericResponse<Void> response = GenericResponse.<Void>builder()
                .mensaje("Usuario eliminado exitosamente")
                .status(HttpStatus.OK.value())
                .codigo(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }
}
