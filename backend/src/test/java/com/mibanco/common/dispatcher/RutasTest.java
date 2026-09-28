package com.mibanco.common.dispatcher;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RutasTest {

    @Test
    void coincideConRutaExacta() {
        assertThat(Rutas.coincide("/api/tarjetas", "/api/tarjetas")).isTrue();
    }

    @Test
    void coincideConRutaQueContieneVariables() {
        assertThat(Rutas.coincide("/api/tarjetas/{id}", "/api/tarjetas/7")).isTrue();
    }

    @Test
    void noCoincideCuandoDifierenLosSegmentos() {
        assertThat(Rutas.coincide("/api/tarjetas", "/api/tipos-tarjetas")).isFalse();
    }

    @Test
    void noCoincideCuandoElNumeroDeSegmentosEsDistinto() {
        assertThat(Rutas.coincide("/api/tarjetas", "/api/tarjetas/7")).isFalse();
    }

    @Test
    void noCoincideCuandoLaVariableEstaVacia() {
        assertThat(Rutas.coincide("/api/tarjetas/{id}", "/api/tarjetas/")).isFalse();
    }

    @Test
    void extraeElValorDeUnaVariable() {
        assertThat(Rutas.variable("/api/tarjetas/{id}", "/api/tarjetas/7", "id")).contains("7");
    }

    @Test
    void noExtraeVariablesSiElPatronNoCoincide() {
        assertThat(Rutas.variable("/api/tarjetas/{id}", "/api/tipos-tarjetas", "id")).isEmpty();
    }

    @Test
    void noExtraeVariablesInexistentesEnElPatron() {
        assertThat(Rutas.variable("/api/tarjetas", "/api/tarjetas", "id")).isEmpty();
    }
}
