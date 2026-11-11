package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.ApiResponse;
import com.proyecto.servicios.model.ClienteActualizacionRequest;
import com.proyecto.servicios.model.ClienteBajaRequest;
import com.proyecto.servicios.model.ClienteFiltroRequest;
import com.proyecto.servicios.model.ClienteRegistroRequest;
import com.proyecto.servicios.model.ClienteResponse;
import com.proyecto.servicios.service.ClienteService;
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
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "API para el onboarding, consulta dinámica, actualización parcial y baja lógica de clientes personas físicas")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Operation(summary = "Registrar un nuevo cliente persona física (crea automáticamente cuenta bancaria y usuario de acceso)")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ClienteResponse>> registrarCliente(
            @Valid @RequestBody ClienteRegistroRequest request) {
        ClienteResponse response = clienteService.registrarCliente(request);
        return new ResponseEntity<>(ApiResponse.created("Cliente registrado exitosamente.", response), HttpStatus.CREATED);
    }

    @Operation(summary = "Consultar clientes combinando diferentes criterios de búsqueda en un único endpoint (GET /clientes)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> consultarClientesGet(
            @Valid @RequestBody(required = false) ClienteFiltroRequest request) {
        List<ClienteResponse> clientes = clienteService.filtrarClientes(request);
        return ResponseEntity.ok(ApiResponse.ok("Consulta de clientes realizada exitosamente.", clientes));
    }

    @Operation(summary = "Consultar clientes combinando diferentes criterios de búsqueda en un único endpoint (POST /clientes/filtro)")
    @PostMapping(value = "/filtro", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> consultarClientesPost(
            @Valid @RequestBody(required = false) ClienteFiltroRequest request) {
        List<ClienteResponse> clientes = clienteService.filtrarClientes(request);
        return ResponseEntity.ok(ApiResponse.ok("Consulta de clientes realizada exitosamente.", clientes));
    }

    @Operation(summary = "Actualizar parcialmente la información de un cliente (PATCH /clientes)")
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ClienteResponse>> actualizarParcialmenteClientePatch(
            @Valid @RequestBody ClienteActualizacionRequest request) {
        ClienteResponse response = clienteService.actualizarCliente(request);
        return ResponseEntity.ok(ApiResponse.ok("Cliente actualizado exitosamente.", response));
    }

    @Operation(summary = "Actualizar parcialmente la información de un cliente (PUT /clientes)")
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ClienteResponse>> actualizarParcialmenteClientePut(
            @Valid @RequestBody ClienteActualizacionRequest request) {
        ClienteResponse response = clienteService.actualizarCliente(request);
        return ResponseEntity.ok(ApiResponse.ok("Cliente actualizado exitosamente.", response));
    }

    @Operation(summary = "Baja lógica de un cliente (desactiva cliente, usuario y cuentas sin eliminar físicamente)")
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ClienteResponse>> darBajaLogicaDelete(
            @Valid @RequestBody ClienteBajaRequest request) {
        ClienteResponse response = clienteService.darBajaLogica(request);
        return ResponseEntity.ok(ApiResponse.ok("Baja lógica de cliente aplicada exitosamente.", response));
    }

    @Operation(summary = "Baja lógica de un cliente mediante PATCH /clientes/baja")
    @PatchMapping(value = "/baja", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ClienteResponse>> darBajaLogicaPatch(
            @Valid @RequestBody ClienteBajaRequest request) {
        ClienteResponse response = clienteService.darBajaLogica(request);
        return ResponseEntity.ok(ApiResponse.ok("Baja lógica de cliente aplicada exitosamente.", response));
    }
}
