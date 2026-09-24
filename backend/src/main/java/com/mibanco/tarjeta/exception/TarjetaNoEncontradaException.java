package com.mibanco.tarjeta.exception;

/**
 * Excepción de negocio: no existe una tarjeta con el identificador consultado (requisito 3.1).
 * Código de error para el cuerpo de respuesta {@code {codigo, mensaje}}: {@link #CODIGO} (HTTP 404 en la capa web).
 */
public class TarjetaNoEncontradaException extends RuntimeException {

    public static final String CODIGO = "TARJETA_NO_ENCONTRADA";

    public TarjetaNoEncontradaException() {
        super("Tarjeta no encontrada");
    }

    public String getCodigo() {
        return CODIGO;
    }
}