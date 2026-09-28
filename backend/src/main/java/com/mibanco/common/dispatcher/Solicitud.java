package com.mibanco.common.dispatcher;

import com.mibanco.common.dispatcher.exception.ParametroInvalidoException;
import org.springframework.http.HttpMethod;

import java.util.Optional;

public record Solicitud(HttpMethod metodo, String ruta, String cuerpo) {

    public Optional<String> variable(String patron, String nombre) {
        return Rutas.variable(patron, ruta, nombre);
    }

    public long variableNumerica(String patron, String nombre) {
        String valor = variable(patron, nombre)
                .orElseThrow(() -> new ParametroInvalidoException(nombre));
        try {
            return Long.parseLong(valor);
        } catch (NumberFormatException e) {
            throw new ParametroInvalidoException(nombre);
        }
    }
}
