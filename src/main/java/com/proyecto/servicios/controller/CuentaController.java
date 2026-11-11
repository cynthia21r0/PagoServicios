package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.CuentaActualizacionRequest;
import com.proyecto.servicios.model.CuentaRequest;
import com.proyecto.servicios.model.CuentaResponse;
import com.proyecto.servicios.model.SaldoCuentaResponse;
import com.proyecto.servicios.service.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cuentas")
@Tag(name = "Cuentas", description = "API para creación, consulta y actualización parcial de cuentas bancarias")
public class CuentaController {

    @Autowired
    private CuentaService cuentaService;

    @Operation(summary = "Consultar una cuenta por número de cuenta")
    @GetMapping(value = "/{numeroCuenta}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CuentaResponse> consultarPorNumeroCuenta(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.consultarPorNumeroCuenta(numeroCuenta));
    }

    @Operation(summary = "Consultar cuentas (por clienteId, por estatus, o todas)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CuentaResponse>> consultarCuentas(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String estatus) {

        if (clienteId != null) {
            return ResponseEntity.ok(cuentaService.consultarPorClienteId(clienteId));
        }
        if (estatus != null && !estatus.isBlank()) {
            return ResponseEntity.ok(cuentaService.consultarPorEstatus(estatus));
        }
        return ResponseEntity.ok(cuentaService.consultarTodas());
    }

    @Operation(summary = "Consultar todas las cuentas activas")
    @GetMapping(value = "/activas", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CuentaResponse>> consultarCuentasActivas() {
        return ResponseEntity.ok(cuentaService.consultarCuentasActivas());
    }

    @Operation(summary = "Consultar el saldo disponible de una cuenta por su número de cuenta")
    @GetMapping(value = "/{numeroCuenta}/saldo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SaldoCuentaResponse> consultarSaldo(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.consultarSaldo(numeroCuenta));
    }

    @Operation(summary = "Crear una nueva cuenta bancaria asociada a un cliente activo")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CuentaResponse> crearCuenta(@RequestBody CuentaRequest request) {
        CuentaResponse response = cuentaService.crearCuenta(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar parcialmente la información de una cuenta (PATCH)")
    @PatchMapping(value = "/{numeroCuenta}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CuentaResponse> actualizarCuentaPatch(
            @PathVariable String numeroCuenta,
            @RequestBody CuentaActualizacionRequest request) {
        return ResponseEntity.ok(cuentaService.actualizarCuenta(numeroCuenta, request));
    }

    @Operation(summary = "Actualizar parcialmente la información de una cuenta (PUT)")
    @PutMapping(value = "/{numeroCuenta}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CuentaResponse> actualizarCuentaPut(
            @PathVariable String numeroCuenta,
            @RequestBody CuentaActualizacionRequest request) {
        return ResponseEntity.ok(cuentaService.actualizarCuenta(numeroCuenta, request));
    }
}

