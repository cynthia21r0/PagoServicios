package com.proyecto.servicios.service;

import com.proyecto.servicios.model.LoginRequest;
import com.proyecto.servicios.model.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}

