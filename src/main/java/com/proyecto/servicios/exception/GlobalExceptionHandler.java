package com.proyecto.servicios.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
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
        return buildResponse(HttpStatus.BAD_REQUEST, "Regla de negocio incumplida", ex.getMessage(), request.getRequestURI(), null);
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
        String mensaje = errores.isEmpty()
                ? "Uno o más campos de la solicitud no cumplen con las validaciones requeridas."
                : "Los datos enviados contienen errores de validación: " + errores.values().iterator().next();
        return buildResponse(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", mensaje, request.getRequestURI(), errores);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> errores.put(v.getPropertyPath().toString(), v.getMessage()));
        return buildResponse(HttpStatus.BAD_REQUEST, "Restricción de validación incumplida",
                "Los parámetros proporcionados no cumplen con el formato requerido.", request.getRequestURI(), errores);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Violación de integridad de datos en {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflicto de integridad de datos",
                "No fue posible completar la operación porque los datos infringen una restricción de unicidad o integridad.",
                request.getRequestURI(), null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Cuerpo de solicitud malformado",
                "El cuerpo de la petición JSON es obligatorio y debe tener una estructura y tipos de datos válidos.",
                request.getRequestURI(), null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no permitido",
                "El método HTTP utilizado no está soportado para este recurso.",
                request.getRequestURI(), null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de contenido no soportado",
                "El formato del contenido debe ser application/json.",
                request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado al procesar solicitud en {}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno en el procesamiento de la solicitud",
                "La operación no pudo completarse debido a una condición inesperada en el servidor. Por favor, verifique su solicitud o intente más tarde.",
                request.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String mensaje, String path, Map<String, String> detalles) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .exito(false)
                .error(error)
                .mensaje(mensaje)
                .path(path)
                .detallesValidacion(detalles)
                .build();
        return new ResponseEntity<>(response, status);
    }
}
