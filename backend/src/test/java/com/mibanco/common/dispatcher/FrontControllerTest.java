package com.mibanco.common.dispatcher;

import com.mibanco.common.dispatcher.exception.DispatcherExceptionHandler;
import com.mibanco.tarjeta.dto.TarjetaResponse;
import com.mibanco.tarjeta.dto.TipoTarjetaResponse;
import com.mibanco.tarjeta.exception.TarjetaExceptionHandler;
import com.mibanco.tarjeta.exception.TarjetaNoEncontradaException;
import com.mibanco.tarjeta.exception.TipoTarjetaNoEncontradoException;
import com.mibanco.tarjeta.handler.CrearTarjetaHandler;
import com.mibanco.tarjeta.handler.ListarTiposTarjetaHandler;
import com.mibanco.tarjeta.handler.ObtenerTarjetaHandler;
import com.mibanco.tarjeta.service.TarjetaService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FrontControllerTest {

    private TarjetaService tarjetaService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        tarjetaService = mock(TarjetaService.class);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        DeserializadorCuerpo deserializadorCuerpo = new DeserializadorCuerpo(JsonMapper.builder().build(), validator);
        FrontController frontController = new FrontController(List.of(
                new CrearTarjetaHandler(tarjetaService, deserializadorCuerpo),
                new ObtenerTarjetaHandler(tarjetaService),
                new ListarTiposTarjetaHandler(tarjetaService)));
        mockMvc = MockMvcBuilders.standaloneSetup(frontController)
                .setControllerAdvice(new DispatcherExceptionHandler(), new TarjetaExceptionHandler())
                .build();
    }

    @Test
    void crearTarjetaConDatosValidosResponde201ConLaTarjetaCreada() throws Exception {
        when(tarjetaService.crearTarjeta(any()))
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
    void crearTarjetaConCuerpoJsonInvalidoResponde400ConCUERPO_INVALIDO() throws Exception {
        mockMvc.perform(post("/api/tarjetas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombrePropietario\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CUERPO_INVALIDO"))
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la petición no es un JSON válido"));
    }

    @Test
    void crearTarjetaConTipoInexistenteResponde400ConTIPO_NO_EXISTE() throws Exception {
        when(tarjetaService.crearTarjeta(any()))
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
    void consultarTarjetaConIdentificadorNoNumericoResponde400ConPARAMETRO_INVALIDO() throws Exception {
        mockMvc.perform(get("/api/tarjetas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"))
                .andExpect(jsonPath("$.mensaje").value("El parámetro id de la ruta debe ser numérico"));
    }

    @Test
    void consultarRutaDesconocidaResponde404ConRUTA_NO_ENCONTRADA() throws Exception {
        mockMvc.perform(get("/api/desconocido"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RUTA_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.mensaje").value("No existe ningún recurso en la ruta /api/desconocido"));
    }

    @Test
    void consultarConMetodoNoSoportadoResponde405ConMETODO_NO_PERMITIDO() throws Exception {
        mockMvc.perform(put("/api/tarjetas"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.codigo").value("METODO_NO_PERMITIDO"))
                .andExpect(jsonPath("$.mensaje").value("El método PUT no está permitido en la ruta /api/tarjetas"));
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
