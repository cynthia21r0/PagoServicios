package com.proyecto.servicios.security;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class BCryptPasswordEncoder {

    private final int strength;
    private final SecureRandom random;

    public BCryptPasswordEncoder() {
        this(10);
    }

    public BCryptPasswordEncoder(int strength) {
        this.strength = strength;
        this.random = new SecureRandom();
    }

    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("La contraseña no puede ser nula");
        }
        String salt = BCrypt.gensalt(this.strength, this.random);
        return BCrypt.hashpw(rawPassword.toString(), salt);
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
    }
}

