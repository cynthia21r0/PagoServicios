package com.proyecto.servicios.client;

import com.proyecto.servicios.model.gestopago.GestoPagoAuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "gestoPagoAuth", url = "${gestopago.auth.url}")
public interface GestoPagoAuthClient {

    @PostMapping(value = "/sistema/app/jwt-gp/authenticate/", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    GestoPagoAuthResponse authenticate(@RequestBody Map<String, ?> formParams);
}