package com.proyecto.servicios.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClienteNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleClienteNoEncontrado(ClienteNoEncontradoException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Cliente no encontrado", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(CuentaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleCuentaNoEncontrada(CuentaNoEncontradaException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Cuenta no encontrada", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Usuario no encontrado", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ClienteYaRegistradoException.class)
    public ResponseEntity<ErrorResponse> handleClienteYaRegistrado(ClienteYaRegistradoException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Cliente ya registrado", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(CurpDuplicadaException.class)
    public ResponseEntity<ErrorResponse> handleCurpDuplicada(CurpDuplicadaException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "CURP duplicada", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(RfcDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleRfcDuplicado(RfcDuplicadoException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "RFC duplicado", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleCorreoDuplicado(CorreoDuplicadoException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Correo electrónico duplicado", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(ValidacionException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validación", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(PasswordInvalidaException.class)
    public ResponseEntity<ErrorResponse> handlePasswordInvalida(PasswordInvalidaException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Contraseña inválida", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> handleCredencialesInvalidas(CredencialesInvalidasException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(UsuarioInactivoException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioInactivo(UsuarioInactivoException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Usuario inactivo", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errores.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        String mensaje = errores.isEmpty() ? "Error de validación en los datos enviados" : errores.values().iterator().next();
        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validación", mensaje, request.getRequestURI(), errores);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> errores.put(v.getPropertyPath().toString(), v.getMessage()));
        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validación", ex.getMessage(), request.getRequestURI(), errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Formato de petición inválido", "El cuerpo de la petición no tiene un formato JSON válido o contiene tipos de datos incorrectos", request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", ex.getMessage(), request.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String mensaje, String path, Map<String, String> detalles) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .mensaje(mensaje)
                .path(path)
                .detallesValidacion(detalles)
                .build();
        return new ResponseEntity<>(response, status);
    }
}

