package com.mibanco.common.dispatcher.exception;

import com.mibanco.common.web.ErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class DispatcherExceptionHandler {

    @ExceptionHandler(RutaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleRutaNoEncontrada(RutaNoEncontradaException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(MetodoNoPermitidoException.class)
    public ResponseEntity<ErrorResponse> handleMetodoNoPermitido(MetodoNoPermitidoException ex) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(CuerpoInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleCuerpoInvalido(CuerpoInvalidoException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(ParametroInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleParametroInvalido(ParametroInvalidoException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getCodigo(), ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String codigo, String mensaje) {
        return ResponseEntity.status(status).body(new ErrorResponse(codigo, mensaje));
    }
}
