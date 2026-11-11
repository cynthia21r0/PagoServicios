package com.proyecto.servicios.service;

import com.proyecto.servicios.model.CuentaActualizacionRequest;
import com.proyecto.servicios.model.CuentaRequest;
import com.proyecto.servicios.model.CuentaResponse;
import com.proyecto.servicios.model.SaldoCuentaResponse;

import java.util.List;

public interface CuentaService {

    CuentaResponse consultarPorNumeroCuenta(String numeroCuenta);

    List<CuentaResponse> consultarPorClienteId(Long clienteId);

    List<CuentaResponse> consultarPorEstatus(String estatus);

    List<CuentaResponse> consultarCuentasActivas();

    List<CuentaResponse> consultarTodas();

    SaldoCuentaResponse consultarSaldo(String numeroCuenta);

    CuentaResponse crearCuenta(CuentaRequest request);

    CuentaResponse actualizarCuenta(String numeroCuenta, CuentaActualizacionRequest request);
}

