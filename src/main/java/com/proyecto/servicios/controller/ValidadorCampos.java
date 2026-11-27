package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.response.GenericResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ValidadorCampos {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResponseEntity<GenericResponse> dtoValidation(MethodArgumentNotValidException errors) {
        GenericResponse genericResponse = new GenericResponse();
        Map<String, String> listError = new HashMap<>();
        for (FieldError fieldError : errors.getFieldErrors()) {
            listError.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        genericResponse.setCodigo(1);
        genericResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        genericResponse.setMensaje("Datos no validos" + listError);
        genericResponse.setErrores(listError);
        return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
    }
}
