package com.mibanco.tarjeta.handler;

import com.mibanco.common.dispatcher.DeserializadorCuerpo;
import com.mibanco.common.dispatcher.Handler;
import com.mibanco.common.dispatcher.RespuestaHandler;
import com.mibanco.common.dispatcher.Rutas;
import com.mibanco.common.dispatcher.Solicitud;
import com.mibanco.tarjeta.dto.TarjetaRequest;
import com.mibanco.tarjeta.service.TarjetaService;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class CrearTarjetaHandler implements Handler {

    private static final String RUTA = "/api/tarjetas";

    private final TarjetaService tarjetaService;
    private final DeserializadorCuerpo deserializadorCuerpo;

    public CrearTarjetaHandler(TarjetaService tarjetaService, DeserializadorCuerpo deserializadorCuerpo) {
        this.tarjetaService = tarjetaService;
        this.deserializadorCuerpo = deserializadorCuerpo;
    }

    @Override
    public boolean soportaRuta(String ruta) {
        return Rutas.coincide(RUTA, ruta);
    }

    @Override
    public boolean soportaMetodo(HttpMethod metodo) {
        return HttpMethod.POST.equals(metodo);
    }

    @Override
    public RespuestaHandler manejar(Solicitud peticion) {
        TarjetaRequest request = deserializadorCuerpo.deserializarYValidar(peticion.cuerpo(), TarjetaRequest.class);
        return RespuestaHandler.creado(tarjetaService.crearTarjeta(request));
    }
}
