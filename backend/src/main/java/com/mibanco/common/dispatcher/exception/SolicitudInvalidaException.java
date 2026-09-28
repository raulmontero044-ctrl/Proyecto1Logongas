package com.mibanco.common.dispatcher.exception;

public class SolicitudInvalidaException extends RuntimeException {

    public static final String CODIGO = "VALIDACION";

    private final String campo;

    public SolicitudInvalidaException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCodigo() {
        return CODIGO;
    }

    public String getCampo() {
        return campo;
    }
}
