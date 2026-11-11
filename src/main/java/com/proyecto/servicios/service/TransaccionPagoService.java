package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.sf.Usuario;
import com.proyecto.servicios.model.ConciliacionTransaccionRequest;
import com.proyecto.servicios.model.PagoServicioRequest;
import com.proyecto.servicios.model.PagoServicioResponse;
import com.proyecto.servicios.model.TransaccionFiltroRequest;

import java.util.List;

public interface TransaccionPagoService {

    PagoServicioResponse procesarPagoServicio(PagoServicioRequest request, Usuario usuarioAutenticado);

    PagoServicioResponse conciliarTransaccion(ConciliacionTransaccionRequest request);

    List<PagoServicioResponse> filtrarTransacciones(TransaccionFiltroRequest filtro);
}

