package com.mibanco.common.css;

import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public final class GeneradorPaletaCss {

    private static final int CANTIDAD_COLORES = 9;
    private static final Pattern CODIGO_HEXADECIMAL = Pattern.compile("#[0-9A-Fa-f]{6}");

    private GeneradorPaletaCss() {
    }

    public static String generar(String nombre, List<String> colores) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (colores == null) {
            throw new IllegalArgumentException("La lista de colores no puede ser nula");
        }
        if (colores.size() != CANTIDAD_COLORES) {
            throw new IllegalArgumentException("La lista debe contener exactamente nueve colores");
        }

        StringJoiner propiedades = new StringJoiner(System.lineSeparator());
        for (int indice = 0; indice < CANTIDAD_COLORES; indice++) {
            String color = colores.get(indice);
            if (color == null || !CODIGO_HEXADECIMAL.matcher(color).matches()) {
                throw new IllegalArgumentException(
                        "El color en la posición " + (indice + 1)
                                + " no es un código hexadecimal válido: " + color
                );
            }
            propiedades.add("  --mlt-sys-color-" + nombre + "-" + (indice + 1) + ": " + color + ";");
        }
        return propiedades.toString();
    }
}
