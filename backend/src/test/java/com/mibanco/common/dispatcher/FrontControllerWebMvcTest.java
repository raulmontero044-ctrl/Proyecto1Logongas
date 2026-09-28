package com.mibanco.common.dispatcher;

import com.mibanco.tarjeta.dto.TarjetaResponse;
import com.mibanco.tarjeta.dto.TipoTarjetaResponse;
import com.mibanco.tarjeta.handler.CrearTarjetaHandler;
import com.mibanco.tarjeta.handler.ListarTiposTarjetaHandler;
import com.mibanco.tarjeta.handler.ObtenerTarjetaHandler;
import com.mibanco.tarjeta.service.TarjetaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FrontController.class)
@Import({DeserializadorCuerpo.class, CrearTarjetaHandler.class, ObtenerTarjetaHandler.class,
        ListarTiposTarjetaHandler.class})
class FrontControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TarjetaService tarjetaService;

    @Test
    void elContextoInyectaLosHandlersRegistradosEnElFrontController() throws Exception {
        when(tarjetaService.listarTiposTarjeta())
                .thenReturn(List.of(new TipoTarjetaResponse(1L, "Débito"), new TipoTarjetaResponse(2L, "Crédito")));

        mockMvc.perform(get("/api/tipos-tarjetas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Débito"))
                .andExpect(jsonPath("$[1].nombre").value("Crédito"));
    }

    @Test
    void elContextoResuelveLasRutasDesconocidasConElFormatoDeErrorDelProyecto() throws Exception {
        mockMvc.perform(get("/api/desconocido"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RUTA_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.mensaje").value("No existe ningún recurso en la ruta /api/desconocido"));
    }

    @Test
    void elContextoDeserializaYValidaElCuerpoDeLaPeticion() throws Exception {
        when(tarjetaService.crearTarjeta(any()))
                .thenReturn(new TarjetaResponse(3L, "Ana Pérez", "Crédito"));

        mockMvc.perform(post("/api/tarjetas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombrePropietario\":\"Ana Pérez\",\"tipo\":\"Crédito\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.nombrePropietario").value("Ana Pérez"));
    }

    @Test
    void elContextoRechazaUnCuerpoQueNoCumpleLaValidacionDelDto() throws Exception {
        mockMvc.perform(post("/api/tarjetas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombrePropietario\":\"  \",\"tipo\":\"Débito\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("NOMBRE_REQUERIDO"))
                .andExpect(jsonPath("$.mensaje").value("El nombre del propietario es obligatorio"));
    }
}
