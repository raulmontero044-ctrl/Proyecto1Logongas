package com.mibanco.common.css;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeneradorPaletaCssTest {

    private static final List<String> COLORES_ROJOS = List.of(
            "#190501",
            "#4D0D05",
            "#83150B",
            "#B41813",
            "#E61919",
            "#EC534B",
            "#F48A80",
            "#FABCB2",
            "#FEEAE6"
    );

    @Test
    void generaLasNuevePropiedadesCssEnElOrdenRecibido() {
        String resultado = GeneradorPaletaCss.generar("rojo", COLORES_ROJOS);

        String resultadoEsperado = String.join(System.lineSeparator(),
                "  --mlt-sys-color-rojo-1: #190501;",
                "  --mlt-sys-color-rojo-2: #4D0D05;",
                "  --mlt-sys-color-rojo-3: #83150B;",
                "  --mlt-sys-color-rojo-4: #B41813;",
                "  --mlt-sys-color-rojo-5: #E61919;",
                "  --mlt-sys-color-rojo-6: #EC534B;",
                "  --mlt-sys-color-rojo-7: #F48A80;",
                "  --mlt-sys-color-rojo-8: #FABCB2;",
                "  --mlt-sys-color-rojo-9: #FEEAE6;"
        );

        assertThat(resultado).isEqualTo(resultadoEsperado);
    }

    @Test
    void rechazaUnNombreVacio() {
        assertThatThrownBy(() -> GeneradorPaletaCss.generar(" ", COLORES_ROJOS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre no puede estar vacío");
    }

    @Test
    void rechazaUnaPaletaQueNoTieneNueveColores() {
        List<String> colores = COLORES_ROJOS.subList(0, 8);

        assertThatThrownBy(() -> GeneradorPaletaCss.generar("rojo", colores))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La lista debe contener exactamente nueve colores");
    }

    @Test
    void rechazaUnColorQueNoEsHexadecimal() {
        List<String> colores = List.of(
                "#190501",
                "#4D0D05",
                "#83150B",
                "#B41813",
                "#E61919",
                "#EC534B",
                "#F48A80",
                "#FABCB2",
                "rojo"
        );

        assertThatThrownBy(() -> GeneradorPaletaCss.generar("rojo", colores))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El color en la posición 9 no es un código hexadecimal válido: rojo");
    }
}
