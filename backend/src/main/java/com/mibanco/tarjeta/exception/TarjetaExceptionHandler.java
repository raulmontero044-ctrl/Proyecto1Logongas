package com.mibanco.tarjeta.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TarjetaExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(TarjetaExceptionHandler.class);

    @ExceptionHandler(NombrePropietarioRequeridoException.class)
    public ResponseEntity<ErrorResponse> handleNombrePropietarioRequerido(NombrePropietarioRequeridoException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(TipoTarjetaNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleTipoTarjetaNoEncontrado(TipoTarjetaNoEncontradoException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(TarjetaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleTarjetaNoEncontrada(TarjetaNoEncontradaException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        FieldError error = ex.getBindingResult().getFieldError();
        if (error != null && "nombrePropietario".equals(error.getField())) {
            return error(HttpStatus.BAD_REQUEST, NombrePropietarioRequeridoException.CODIGO,
                    "El nombre del propietario es obligatorio");
        }
        String mensaje = error != null ? error.getDefaultMessage() : "Datos de entrada inválidos";
        return error(HttpStatus.BAD_REQUEST, "VALIDACION", mensaje);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInesperado(Exception ex) {
        log.error("Error inesperado procesando una petición de tarjeta", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ha ocurrido un error inesperado");
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String codigo, String mensaje) {
        return ResponseEntity.status(status).body(new ErrorResponse(codigo, mensaje));
    }

    public record ErrorResponse(String codigo, String mensaje) {
    }
}