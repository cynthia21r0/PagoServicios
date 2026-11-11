package com.proyecto.servicios.util;

import com.proyecto.servicios.exception.PasswordInvalidaException;
import com.proyecto.servicios.exception.ValidacionException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public final class ValidadorNegocio {

    private static final Pattern PATRON_NOMBRE = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]{2,50}$");
    private static final Pattern PATRON_CURP = Pattern.compile("^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]\\d$");
    private static final Pattern PATRON_RFC = Pattern.compile("^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$");
    private static final Pattern PATRON_CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^\\d{10}$");
    private static final Pattern PATRON_CODIGO_POSTAL = Pattern.compile("^\\d{5}$");

    private static final Pattern PATRON_MAYUSCULA = Pattern.compile(".*[A-Z].*");
    private static final Pattern PATRON_MINUSCULA = Pattern.compile(".*[a-z].*");
    private static final Pattern PATRON_NUMERO = Pattern.compile(".*\\d.*");
    private static final Pattern PATRON_ESPECIAL = Pattern.compile(".*[^a-zA-Z0-9].*");

    private ValidadorNegocio() {
    }

    public static void validarNombreOApellido(String valor, String nombreCampo, boolean obligatorio) {
        if (valor == null || valor.trim().isEmpty()) {
            if (obligatorio) {
                throw new ValidacionException("El campo '" + nombreCampo + "' es obligatorio.");
            }
            return;
        }
        String limpio = valor.trim();
        if (limpio.length() < 2 || limpio.length() > 50) {
            throw new ValidacionException("El campo '" + nombreCampo + "' debe tener una longitud entre 2 y 50 caracteres.");
        }
        if (!PATRON_NOMBRE.matcher(limpio).matches()) {
            throw new ValidacionException("El campo '" + nombreCampo + "' solo puede contener letras y espacios.");
        }
    }

    public static void validarCurp(String curp) {
        if (curp == null || curp.trim().isEmpty()) {
            throw new ValidacionException("La CURP es obligatoria.");
        }
        String limpia = curp.trim().toUpperCase();
        if (limpia.length() != 18) {
            throw new ValidacionException("La CURP debe contener exactamente 18 caracteres.");
        }
        if (!PATRON_CURP.matcher(limpia).matches()) {
            throw new ValidacionException("La CURP no cumple con el formato oficial válido.");
        }
    }

    public static void validarRfc(String rfc) {
        if (rfc == null || rfc.trim().isEmpty()) {
            throw new ValidacionException("El RFC es obligatorio.");
        }
        String limpio = rfc.trim().toUpperCase();
        if (limpio.length() != 12 && limpio.length() != 13) {
            throw new ValidacionException("El RFC debe contener 12 o 13 caracteres.");
        }
        if (!PATRON_RFC.matcher(limpio).matches()) {
            throw new ValidacionException("El RFC no cumple con el formato oficial válido.");
        }
    }

    public static void validarCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            throw new ValidacionException("El correo electrónico es obligatorio.");
        }
        String limpio = correo.trim();
        if (limpio.length() > 100) {
            throw new ValidacionException("El correo electrónico no puede exceder los 100 caracteres.");
        }
        if (!PATRON_CORREO.matcher(limpio).matches()) {
            throw new ValidacionException("El correo electrónico no tiene un formato válido.");
        }
    }

    public static void validarFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            throw new ValidacionException("La fecha de nacimiento es obligatoria.");
        }
        LocalDate hoy = LocalDate.now();
        if (fechaNacimiento.isAfter(hoy)) {
            throw new ValidacionException("La fecha de nacimiento no puede ser una fecha futura.");
        }
        int edad = Period.between(fechaNacimiento, hoy).getYears();
        if (edad < 18) {
            throw new ValidacionException("El cliente debe ser mayor de edad (18 años o más).");
        }
    }

    public static void validarTelefono(String telefono, String nombreCampo, boolean obligatorio) {
        if (telefono == null || telefono.trim().isEmpty()) {
            if (obligatorio) {
                throw new ValidacionException("El campo '" + nombreCampo + "' es obligatorio.");
            }
            return;
        }
        if (!PATRON_TELEFONO.matcher(telefono.trim()).matches()) {
            throw new ValidacionException("El campo '" + nombreCampo + "' debe contener exactamente 10 dígitos numéricos.");
        }
    }

    public static void validarCodigoPostal(String codigoPostal) {
        if (codigoPostal == null || codigoPostal.trim().isEmpty()) {
            throw new ValidacionException("El código postal es obligatorio.");
        }
        if (!PATRON_CODIGO_POSTAL.matcher(codigoPostal.trim()).matches()) {
            throw new ValidacionException("El código postal debe contener exactamente 5 dígitos numéricos.");
        }
    }

    public static void validarIngresoMensual(BigDecimal ingresoMensual) {
        if (ingresoMensual == null) {
            throw new ValidacionException("El ingreso mensual es obligatorio.");
        }
        if (ingresoMensual.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionException("El ingreso mensual debe ser mayor a cero.");
        }
    }

    public static void validarSaldoCuenta(BigDecimal saldo) {
        if (saldo == null) {
            throw new ValidacionException("El saldo de la cuenta es obligatorio.");
        }
        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionException("El saldo de la cuenta no puede ser negativo.");
        }
    }

    public static void validarCampoObligatorio(String valor, String nombreCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacionException("El campo '" + nombreCampo + "' es obligatorio.");
        }
    }

    public static void validarPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new PasswordInvalidaException("La contraseña es obligatoria.");
        }
        if (password.length() < 8) {
            throw new PasswordInvalidaException("La contraseña debe contener mínimo 8 caracteres.");
        }
        if (!PATRON_MAYUSCULA.matcher(password).matches()) {
            throw new PasswordInvalidaException("La contraseña debe contener al menos una letra mayúscula.");
        }
        if (!PATRON_MINUSCULA.matcher(password).matches()) {
            throw new PasswordInvalidaException("La contraseña debe contener al menos una letra minúscula.");
        }
        if (!PATRON_NUMERO.matcher(password).matches()) {
            throw new PasswordInvalidaException("La contraseña debe contener al menos un número.");
        }
        if (!PATRON_ESPECIAL.matcher(password).matches()) {
            throw new PasswordInvalidaException("La contraseña debe contener al menos un carácter especial.");
        }
    }
}

