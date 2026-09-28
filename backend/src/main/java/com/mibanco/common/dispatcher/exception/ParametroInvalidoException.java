package com.mibanco.common.dispatcher.exception;

public class ParametroInvalidoException extends RuntimeException {

    public static final String CODIGO = "PARAMETRO_INVALIDO";

    public ParametroInvalidoException(String nombre) {
        super("El parámetro " + nombre + " de la ruta debe ser numérico");
    }

    public String getCodigo() {
        return CODIGO;
    }
}
