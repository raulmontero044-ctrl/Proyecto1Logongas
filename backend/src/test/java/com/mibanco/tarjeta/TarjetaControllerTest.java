package com.mibanco.tarjeta;

import com.mibanco.tarjeta.controller.TarjetaController;
import com.mibanco.tarjeta.dto.TarjetaRequest;
import com.mibanco.tarjeta.dto.TarjetaResponse;
import com.mibanco.tarjeta.dto.TipoTarjetaResponse;
import com.mibanco.tarjeta.exception.TarjetaNoEncontradaException;
import com.mibanco.tarjeta.exception.TipoTarjetaNoEncontradoException;
import com.mibanco.tarjeta.service.TarjetaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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

@WebMvcTest(TarjetaController.class)
class TarjetaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TarjetaService tarjetaService;

    @Test
    void crearTarjetaConDatosValidosResponde201ConLaTarjetaCreada() throws Exception {
        when(tarjetaService.crearTarjeta(any(TarjetaRequest.class)))
                .thenReturn(new TarjetaResponse(1L, "María López", "Débito"));

        mockMvc.perform(post("/api/tarjetas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombrePropietario\":\"María López\",\"tipo\":\"Débito\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombrePropietario").value("María López"))
                .andExpect(jsonPath("$.tipo").value("Débito"));
    }

    @Test
    void crearTarjetaConNombreDelPropietarioVacioResponde400ConNOMBRE_REQUERIDO() throws Exception {
        mockMvc.perform(post("/api/tarjetas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombrePropietario\":\"   \",\"tipo\":\"Débito\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("NOMBRE_REQUERIDO"))
                .andExpect(jsonPath("$.mensaje").value("El nombre del propietario es obligatorio"));
    }

    @Test
    void crearTarjetaConTipoInexistenteResponde400ConTIPO_NO_EXISTE() throws Exception {
        when(tarjetaService.crearTarjeta(any(TarjetaRequest.class)))
                .thenThrow(new TipoTarjetaNoEncontradoException());

        mockMvc.perform(post("/api/tarjetas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombrePropietario\":\"Ana Pérez\",\"tipo\":\"Bienvenida\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("TIPO_NO_EXISTE"))
                .andExpect(jsonPath("$.mensaje").value("El tipo de tarjeta no existe"));
    }

    @Test
    void consultarTarjetaExistenteResponde200ConLaTarjeta() throws Exception {
        when(tarjetaService.obtenerTarjeta(7L)).thenReturn(new TarjetaResponse(7L, "María López", "Crédito"));

        mockMvc.perform(get("/api/tarjetas/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.nombrePropietario").value("María López"))
                .andExpect(jsonPath("$.tipo").value("Crédito"));
    }

    @Test
    void consultarTarjetaInexistenteResponde404ConTARJETA_NO_ENCONTRADA() throws Exception {
        when(tarjetaService.obtenerTarjeta(99L)).thenThrow(new TarjetaNoEncontradaException());

        mockMvc.perform(get("/api/tarjetas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("TARJETA_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.mensaje").value("Tarjeta no encontrada"));
    }

    @Test
    void listarTiposDeTarjetaResponde200ConElCatalogo() throws Exception {
        when(tarjetaService.listarTiposTarjeta())
                .thenReturn(List.of(new TipoTarjetaResponse(1L, "Débito"), new TipoTarjetaResponse(2L, "Crédito")));

        mockMvc.perform(get("/api/tipos-tarjetas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombre").value("Débito"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].nombre").value("Crédito"));
    }
}