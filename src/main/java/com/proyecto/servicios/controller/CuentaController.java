package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.ApiResponse;
import com.proyecto.servicios.model.CuentaActualizacionRequest;
import com.proyecto.servicios.model.CuentaFiltroRequest;
import com.proyecto.servicios.model.CuentaRequest;
import com.proyecto.servicios.model.CuentaResponse;
import com.proyecto.servicios.service.CuentaService;
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
@RequestMapping("/cuentas")
@Tag(name = "Cuentas", description = "API para creación, consulta dinámica y actualización parcial de cuentas bancarias")
public class CuentaController {

    @Autowired
    private CuentaService cuentaService;

    @Operation(summary = "Consultar cuentas bancarias combinando filtros dinámicos (GET /cuentas)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<CuentaResponse>>> consultarCuentasGet(
            @Valid @RequestBody(required = false) CuentaFiltroRequest request) {
        List<CuentaResponse> cuentas = cuentaService.filtrarCuentas(request);
        return ResponseEntity.ok(ApiResponse.ok("Consulta de cuentas realizada exitosamente.", cuentas));
    }

    @Operation(summary = "Consultar cuentas bancarias combinando filtros dinámicos (POST /cuentas/filtro)")
    @PostMapping(value = "/filtro", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<CuentaResponse>>> consultarCuentasPost(
            @Valid @RequestBody(required = false) CuentaFiltroRequest request) {
        List<CuentaResponse> cuentas = cuentaService.filtrarCuentas(request);
        return ResponseEntity.ok(ApiResponse.ok("Consulta de cuentas realizada exitosamente.", cuentas));
    }

    @Operation(summary = "Crear una nueva cuenta bancaria asociada a un cliente activo")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CuentaResponse>> crearCuenta(
            @Valid @RequestBody CuentaRequest request) {
        CuentaResponse response = cuentaService.crearCuenta(request);
        return new ResponseEntity<>(ApiResponse.created("Cuenta bancaria creada exitosamente.", response), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar parcialmente la información de una cuenta (PATCH /cuentas)")
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CuentaResponse>> actualizarCuentaPatch(
            @Valid @RequestBody CuentaActualizacionRequest request) {
        CuentaResponse response = cuentaService.actualizarCuenta(request);
        return ResponseEntity.ok(ApiResponse.ok("Cuenta bancaria actualizada exitosamente.", response));
    }

    @Operation(summary = "Actualizar parcialmente la información de una cuenta (PUT /cuentas)")
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CuentaResponse>> actualizarCuentaPut(
            @Valid @RequestBody CuentaActualizacionRequest request) {
        CuentaResponse response = cuentaService.actualizarCuenta(request);
        return ResponseEntity.ok(ApiResponse.ok("Cuenta bancaria actualizada exitosamente.", response));
    }
}
