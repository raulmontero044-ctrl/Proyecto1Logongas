package com.mibanco.common.dispatcher;

import com.mibanco.common.dispatcher.exception.MetodoNoPermitidoException;
import com.mibanco.common.dispatcher.exception.RutaNoEncontradaException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FrontController {

    private static final Logger log = LoggerFactory.getLogger(FrontController.class);

    private final List<Handler> handlers;

    public FrontController(List<Handler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<Object> despachar(HttpServletRequest peticionHttp) throws IOException {
        Solicitud peticion = new Solicitud(HttpMethod.valueOf(peticionHttp.getMethod()), rutaDe(peticionHttp),
                cuerpoDe(peticionHttp));
        Handler handler = resolver(peticion);
        log.debug("Front Controller despacha {} {} a {}", peticion.metodo(), peticion.ruta(),
                handler.getClass().getSimpleName());
        RespuestaHandler respuesta = handler.manejar(peticion);
        return ResponseEntity.status(respuesta.estado()).body(respuesta.cuerpo());
    }

    private Handler resolver(Solicitud peticion) {
        Handler handlerPorRuta = handlers.stream()
                .filter(handler -> handler.soportaRuta(peticion.ruta()))
                .findFirst()
                .orElseThrow(() -> new RutaNoEncontradaException(peticion.ruta()));
        if (!handlerPorRuta.soportaMetodo(peticion.metodo())) {
            throw new MetodoNoPermitidoException(peticion.metodo().name(), peticion.ruta());
        }
        return handlerPorRuta;
    }

    private String rutaDe(HttpServletRequest peticionHttp) {
        return peticionHttp.getRequestURI().substring(peticionHttp.getContextPath().length());
    }

    private String cuerpoDe(HttpServletRequest peticionHttp) throws IOException {
        return new String(peticionHttp.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
