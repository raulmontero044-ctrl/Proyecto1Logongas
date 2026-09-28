package com.mibanco.common.dispatcher.exception;

public class MetodoNoPermitidoException extends RuntimeException {

    public static final String CODIGO = "METODO_NO_PERMITIDO";

    public MetodoNoPermitidoException(String metodo, String ruta) {
        super("El método " + metodo + " no está permitido en la ruta " + ruta);
    }

    public String getCodigo() {
        return CODIGO;
    }
}
