package com.mibanco.tarjeta.exception;

/**
 * Excepción de negocio: el nombre del propietario es obligatorio cuando se crea una tarjeta (requisito 1.3).
 * Código de error para el cuerpo de respuesta {@code {codigo, mensaje}}: {@link #CODIGO} (HTTP 400 en la capa web).
 */
public class NombrePropietarioRequeridoException extends RuntimeException {

    public static final String CODIGO = "NOMBRE_REQUERIDO";

    public NombrePropietarioRequeridoException() {
        super("El nombre del propietario es obligatorio");
    }

    public String getCodigo() {
        return CODIGO;
    }
}