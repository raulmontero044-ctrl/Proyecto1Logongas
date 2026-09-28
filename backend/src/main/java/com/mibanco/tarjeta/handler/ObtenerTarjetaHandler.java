package com.mibanco.tarjeta.handler;

import com.mibanco.common.dispatcher.Handler;
import com.mibanco.common.dispatcher.RespuestaHandler;
import com.mibanco.common.dispatcher.Rutas;
import com.mibanco.common.dispatcher.Solicitud;
import com.mibanco.tarjeta.service.TarjetaService;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class ObtenerTarjetaHandler implements Handler {

    private static final String RUTA = "/api/tarjetas/{id}";

    private final TarjetaService tarjetaService;

    public ObtenerTarjetaHandler(TarjetaService tarjetaService) {
        this.tarjetaService = tarjetaService;
    }

    @Override
    public boolean soportaRuta(String ruta) {
        return Rutas.coincide(RUTA, ruta);
    }

    @Override
    public boolean soportaMetodo(HttpMethod metodo) {
        return HttpMethod.GET.equals(metodo);
    }

    @Override
    public RespuestaHandler manejar(Solicitud peticion) {
        long id = peticion.variableNumerica(RUTA, "id");
        return RespuestaHandler.ok(tarjetaService.obtenerTarjeta(id));
    }
}
