package com.proyecto.servicios.service;

import com.proyecto.servicios.model.transaccion.HistorialTransaccionesRequestDTO;
import com.proyecto.servicios.model.transaccion.PagarServicioRequestDTO;
import com.proyecto.servicios.model.transaccion.TransaccionHistorialDTO;
import com.proyecto.servicios.model.transaccion.TransaccionResponseDTO;

import java.util.List;

public interface TransaccionService {

    TransaccionResponseDTO pagarServicio(PagarServicioRequestDTO request, String correoAutenticado);

    List<TransaccionHistorialDTO> obtenerHistorial(HistorialTransaccionesRequestDTO request, String correoAutenticado);

    com.proyecto.servicios.model.transaccion.TicketResponseDTO generarTicket(Long folioTransaccion, String correoAutenticado);
}