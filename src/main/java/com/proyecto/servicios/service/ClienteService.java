package com.proyecto.servicios.service;

import com.proyecto.servicios.model.ClienteActualizacionRequest;
import com.proyecto.servicios.model.ClienteRegistroRequest;
import com.proyecto.servicios.model.ClienteResponse;

import java.time.LocalDate;
import java.util.List;

public interface ClienteService {

    ClienteResponse registrarCliente(ClienteRegistroRequest request);

    List<ClienteResponse> consultarTodos();

    ClienteResponse consultarPorId(Long id);

    ClienteResponse consultarPorCurp(String curp);

    ClienteResponse consultarPorRfc(String rfc);

    ClienteResponse consultarPorCorreo(String correo);

    ClienteResponse consultarPorNumeroCuenta(String numeroCuenta);

    List<ClienteResponse> buscarPorNombre(String nombre);

    List<ClienteResponse> buscarPorApellidoPaterno(String apellidoPaterno);

    List<ClienteResponse> buscarPorApellidoMaterno(String apellidoMaterno);

    List<ClienteResponse> consultarClientesActivos();

    List<ClienteResponse> consultarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);

    ClienteResponse actualizarCliente(Long id, ClienteActualizacionRequest request);

    ClienteResponse darBajaLogica(Long id);
}

