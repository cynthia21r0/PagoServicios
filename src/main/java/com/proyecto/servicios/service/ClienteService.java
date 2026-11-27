package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.onboarding.catalogos.TipoBaja;
import com.proyecto.servicios.model.onboarding.ClienteActualizaRequest;
import com.proyecto.servicios.model.onboarding.ClienteRegistroRequest;
import com.proyecto.servicios.model.onboarding.dto.ClienteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteService {

    ClienteResponseDTO registrarCliente(ClienteRegistroRequest request);

    Page<ClienteResponseDTO> buscarClientes(Pageable pageable);

    Page<ClienteResponseDTO> buscarClientes(int page, int size);

    Page<ClienteResponseDTO> buscarClientes(String nombre, String apellidoPaterno, String apellidoMaterno, Pageable pageable);

    Page<ClienteResponseDTO> buscarClientes(String nombre, String apellidoPaterno, String apellidoMaterno, int page, int size);

    ClienteResponseDTO buscarCliente(Long id);

    ClienteResponseDTO buscarCliente(String rfc, String curp);

    ClienteResponseDTO obtenerPorId(Long id);
    ClienteResponseDTO obtenerPorCurp(String curp);
    ClienteResponseDTO obtenerPorRfc(String rfc);
    ClienteResponseDTO obtenerPorRfcYCurp(String rfc, String curp);

    ClienteResponseDTO actualizarParcialmente(Long id, ClienteActualizaRequest request);
    void bajaLogica(Long id, TipoBaja tipoBaja);
    void bloquear(Long id);
    void desbloquear(Long id);
}
