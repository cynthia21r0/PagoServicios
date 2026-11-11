package com.proyecto.servicios.controller;

import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.model.ApiResponse;
import com.proyecto.servicios.model.ConciliacionTransaccionRequest;
import com.proyecto.servicios.model.PagoServicioRequest;
import com.proyecto.servicios.model.PagoServicioResponse;
import com.proyecto.servicios.model.TransaccionFiltroRequest;
import com.proyecto.servicios.service.TransaccionPagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transacciones")
@Tag(name = "Transacciones GestoPago", description = "API para ejecución segura de pagos de servicios con GestoPago (sendTx.do), idempotencia, control de concurrencia y conciliación")
public class TransaccionPagoController {

    @Autowired
    private TransaccionPagoService transaccionPagoService;

    @Operation(summary = "Ejecutar pago de servicio validando identidad, saldo, catálogo, idempotencia e integrando con GestoPago sendTx.do")
    @PostMapping(value = "/pagar", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PagoServicioResponse>> pagarServicio(
            @Valid @RequestBody PagoServicioRequest request,
            HttpServletRequest servletRequest) {
        Usuario usuarioAutenticado = (Usuario) servletRequest.getAttribute("usuarioAutenticado");
        PagoServicioResponse response = transaccionPagoService.procesarPagoServicio(request, usuarioAutenticado);
        return ResponseEntity.ok(ApiResponse.ok("Pago de servicio procesado exitosamente.", response));
    }

    @Operation(summary = "Conciliar una transacción que quedó en estado PENDIENTE_CONCILIACION por timeout del proveedor")
    @PostMapping(value = "/conciliar", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PagoServicioResponse>> conciliarTransaccion(
            @Valid @RequestBody ConciliacionTransaccionRequest request) {
        PagoServicioResponse response = transaccionPagoService.conciliarTransaccion(request);
        return ResponseEntity.ok(ApiResponse.ok("Transacción conciliada exitosamente.", response));
    }

    @Operation(summary = "Consultar transacciones combinando filtros dinámicos (POST /transacciones/filtro)")
    @PostMapping(value = "/filtro", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<PagoServicioResponse>>> filtrarTransaccionesPost(
            @Valid @RequestBody(required = false) TransaccionFiltroRequest request) {
        List<PagoServicioResponse> lista = transaccionPagoService.filtrarTransacciones(request);
        return ResponseEntity.ok(ApiResponse.ok("Consulta de transacciones realizada exitosamente.", lista));
    }

    @Operation(summary = "Consultar transacciones combinando filtros dinámicos (GET /transacciones)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<PagoServicioResponse>>> filtrarTransaccionesGet(
            @Valid @RequestBody(required = false) TransaccionFiltroRequest request) {
        List<PagoServicioResponse> lista = transaccionPagoService.filtrarTransacciones(request);
        return ResponseEntity.ok(ApiResponse.ok("Consulta de transacciones realizada exitosamente.", lista));
    }
}

