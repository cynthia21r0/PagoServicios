package com.proyecto.servicios.service;

import com.proyecto.servicios.model.security.OAuthTokenRequest;
import com.proyecto.servicios.model.security.OAuthTokenResponse;

public interface OAuthClientService {

    OAuthTokenResponse emitirToken(OAuthTokenRequest request);
}
