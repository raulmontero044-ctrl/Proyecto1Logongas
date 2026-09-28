package com.mibanco.common.dispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Rutas {

    private Rutas() {
    }

    public static boolean coincide(String patron, String ruta) {
        List<String> segmentosPatron = segmentos(patron);
        List<String> segmentosRuta = segmentos(ruta);
        if (segmentosPatron.size() != segmentosRuta.size()) {
            return false;
        }
        for (int i = 0; i < segmentosPatron.size(); i++) {
            String segmentoPatron = segmentosPatron.get(i);
            if (esVariable(segmentoPatron)) {
                if (segmentosRuta.get(i).isBlank()) {
                    return false;
                }
            } else if (!segmentoPatron.equals(segmentosRuta.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static Optional<String> variable(String patron, String ruta, String nombre) {
        List<String> segmentosPatron = segmentos(patron);
        List<String> segmentosRuta = segmentos(ruta);
        if (segmentosPatron.size() != segmentosRuta.size()) {
            return Optional.empty();
        }
        String marcador = "{" + nombre + "}";
        for (int i = 0; i < segmentosPatron.size(); i++) {
            if (segmentosPatron.get(i).equals(marcador)) {
                return Optional.of(segmentosRuta.get(i));
            }
        }
        return Optional.empty();
    }

    private static boolean esVariable(String segmento) {
        return segmento.length() > 2
                && segmento.charAt(0) == '{'
                && segmento.charAt(segmento.length() - 1) == '}';
    }

    private static List<String> segmentos(String ruta) {
        List<String> segmentos = new ArrayList<>();
        for (String segmento : ruta.split("/")) {
            if (!segmento.isEmpty()) {
                segmentos.add(segmento);
            }
        }
        return segmentos;
    }
}
