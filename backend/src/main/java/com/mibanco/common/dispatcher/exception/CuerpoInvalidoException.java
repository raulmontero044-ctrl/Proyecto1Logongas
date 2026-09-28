package com.mibanco.common.dispatcher.exception;

public class CuerpoInvalidoException extends RuntimeException {

    public static final String CODIGO = "CUERPO_INVALIDO";

    public CuerpoInvalidoException() {
        super("El cuerpo de la petición no es un JSON válido");
    }

    public String getCodigo() {
        return CODIGO;
    }
}
