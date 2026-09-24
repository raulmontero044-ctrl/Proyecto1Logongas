package com.mibanco.tarjeta.exception;

/**
 * Excepción de negocio: el tipo indicado no existe en el catálogo TiposDeTarjetas (requisito 1.4).
 * Código de error para el cuerpo de respuesta {@code {codigo, mensaje}}: {@link #CODIGO} (HTTP 400 en la capa web).
 */
public class TipoTarjetaNoEncontradoException extends RuntimeException {

    public static final String CODIGO = "TIPO_NO_EXISTE";

    public TipoTarjetaNoEncontradoException() {
        super("El tipo de tarjeta no existe");
    }

    public String getCodigo() {
        return CODIGO;
    }
}