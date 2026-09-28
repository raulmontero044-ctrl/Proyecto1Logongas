package com.mibanco.common.dispatcher;

import org.springframework.http.HttpStatus;

public record RespuestaHandler(HttpStatus estado, Object cuerpo) {

    public static RespuestaHandler ok(Object cuerpo) {
        return new RespuestaHandler(HttpStatus.OK, cuerpo);
    }

    public static RespuestaHandler creado(Object cuerpo) {
        return new RespuestaHandler(HttpStatus.CREATED, cuerpo);
    }
}
