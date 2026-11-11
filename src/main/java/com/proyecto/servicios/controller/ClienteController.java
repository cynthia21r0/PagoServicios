package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.ClienteActualizacionRequest;
import com.proyecto.servicios.model.ClienteRegistroRequest;
import com.proyecto.servicios.model.ClienteResponse;
import com.proyecto.servicios.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "API para el onboarding, consulta, actualización parcial y baja lógica de clientes personas físicas")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Operation(summary = "Registrar un nuevo cliente persona física (crea automáticamente cuenta bancaria y usuario de acceso)")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClienteResponse> registrarCliente(@RequestBody ClienteRegistroRequest request) {
        ClienteResponse response = clienteService.registrarCliente(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Consultar clientes (todos o filtrados por nombre, apellidoPaterno, apellidoMaterno, curp, rfc, correo, numeroCuenta, activo o rango de fechas)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> consultarClientes(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellidoPaterno,
            @RequestParam(required = false) String apellidoMaterno,
            @RequestParam(required = false) String curp,
            @RequestParam(required = false) String rfc,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String numeroCuenta,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        if (curp != null && !curp.isBlank()) {
            return ResponseEntity.ok(clienteService.consultarPorCurp(curp));
        }
        if (rfc != null && !rfc.isBlank()) {
            return ResponseEntity.ok(clienteService.consultarPorRfc(rfc));
        }
        if (correo != null && !correo.isBlank()) {
            return ResponseEntity.ok(clienteService.consultarPorCorreo(correo));
        }
        if (numeroCuenta != null && !numeroCuenta.isBlank()) {
            return ResponseEntity.ok(clienteService.consultarPorNumeroCuenta(numeroCuenta));
        }
        if (nombre != null && !nombre.isBlank()) {
            return ResponseEntity.ok(clienteService.buscarPorNombre(nombre));
        }
        if (apellidoPaterno != null && !apellidoPaterno.isBlank()) {
            return ResponseEntity.ok(clienteService.buscarPorApellidoPaterno(apellidoPaterno));
        }
        if (apellidoMaterno != null && !apellidoMaterno.isBlank()) {
            return ResponseEntity.ok(clienteService.buscarPorApellidoMaterno(apellidoMaterno));
        }
        if (fechaInicio != null && fechaFin != null) {
            return ResponseEntity.ok(clienteService.consultarPorRangoFechas(fechaInicio, fechaFin));
        }
        if (Boolean.TRUE.equals(activo)) {
            return ResponseEntity.ok(clienteService.consultarClientesActivos());
        }
        return ResponseEntity.ok(clienteService.consultarTodos());
    }

    @Operation(summary = "Consultar un cliente por su identificador")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClienteResponse> consultarClientePorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.consultarPorId(id));
    }

    @Operation(summary = "Consultar todos los clientes activos")
    @GetMapping(value = "/activos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClienteResponse>> consultarClientesActivos() {
        return ResponseEntity.ok(clienteService.consultarClientesActivos());
    }

    @Operation(summary = "Buscar cliente por CURP")
    @GetMapping(value = "/curp/{curp}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClienteResponse> consultarPorCurp(@PathVariable String curp) {
        return ResponseEntity.ok(clienteService.consultarPorCurp(curp));
    }

    @Operation(summary = "Buscar cliente por RFC")
    @GetMapping(value = "/rfc/{rfc}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClienteResponse> consultarPorRfc(@PathVariable String rfc) {
        return ResponseEntity.ok(clienteService.consultarPorRfc(rfc));
    }

    @Operation(summary = "Buscar cliente por correo electrónico")
    @GetMapping(value = "/correo/{correo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClienteResponse> consultarPorCorreo(@PathVariable String correo) {
        return ResponseEntity.ok(clienteService.consultarPorCorreo(correo));
    }

    @Operation(summary = "Consultar cliente por número de cuenta")
    @GetMapping(value = "/cuenta/{numeroCuenta}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClienteResponse> consultarPorNumeroCuenta(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(clienteService.consultarPorNumeroCuenta(numeroCuenta));
    }

    @Operation(summary = "Obtener clientes registrados en un rango de fechas")
    @GetMapping(value = "/rango-fechas", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClienteResponse>> consultarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(clienteService.consultarPorRangoFechas(fechaInicio, fechaFin));
    }

    @Operation(summary = "Actualizar parcialmente la información de un cliente (PATCH)")
    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClienteResponse> actualizarParcialmenteClientePatch(
            @PathVariable Long id,
            @RequestBody ClienteActualizacionRequest request) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, request));
    }

    @Operation(summary = "Actualizar parcialmente la información de un cliente (PUT)")
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClienteResponse> actualizarParcialmenteClientePut(
            @PathVariable Long id,
            @RequestBody ClienteActualizacionRequest request) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, request));
    }

    @Operation(summary = "Baja lógica de un cliente (desactiva cliente, usuario y cuentas sin eliminar físicamente)")
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClienteResponse> darBajaLogicaDelete(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.darBajaLogica(id));
    }

    @Operation(summary = "Baja lógica de un cliente mediante PATCH /{id}/baja")
    @PatchMapping(value = "/{id}/baja", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClienteResponse> darBajaLogicaPatch(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.darBajaLogica(id));
    }
}

