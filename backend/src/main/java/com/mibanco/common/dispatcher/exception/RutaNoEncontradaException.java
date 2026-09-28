package com.mibanco.common.dispatcher.exception;

public class RutaNoEncontradaException extends RuntimeException {

    public static final String CODIGO = "RUTA_NO_ENCONTRADA";

    public RutaNoEncontradaException(String ruta) {
        super("No existe ningún recurso en la ruta " + ruta);
    }

    public String getCodigo() {
        return CODIGO;
    }
}
