package com.mibanco.common.dispatcher;

import org.springframework.http.HttpMethod;

public interface Handler {

    boolean soportaRuta(String ruta);

    boolean soportaMetodo(HttpMethod metodo);

    RespuestaHandler manejar(Solicitud peticion);
}
