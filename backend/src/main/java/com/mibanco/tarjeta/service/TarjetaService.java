package com.mibanco.tarjeta.service;

import com.mibanco.tarjeta.dto.TarjetaRequest;
import com.mibanco.tarjeta.dto.TarjetaResponse;
import com.mibanco.tarjeta.entity.Tarjeta;
import com.mibanco.tarjeta.entity.TipoTarjeta;
import com.mibanco.tarjeta.exception.NombrePropietarioRequeridoException;
import com.mibanco.tarjeta.exception.TarjetaNoEncontradaException;
import com.mibanco.tarjeta.exception.TipoTarjetaNoEncontradoException;
import com.mibanco.tarjeta.repository.TarjetaRepository;
import com.mibanco.tarjeta.repository.TipoTarjetaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TarjetaService {

    private static final Logger log = LoggerFactory.getLogger(TarjetaService.class);

    private final TarjetaRepository tarjetaRepository;
    private final TipoTarjetaRepository tipoTarjetaRepository;

    public TarjetaService(TarjetaRepository tarjetaRepository, TipoTarjetaRepository tipoTarjetaRepository) {
        this.tarjetaRepository = tarjetaRepository;
        this.tipoTarjetaRepository = tipoTarjetaRepository;
    }

    public TarjetaResponse crearTarjeta(TarjetaRequest request) {
        if (request.nombrePropietario() == null || request.nombrePropietario().isBlank()) {
            log.warn("Rechazo de creación de tarjeta: nombre del propietario vacío");
            throw new NombrePropietarioRequeridoException();
        }

        TipoTarjeta tipo = null;
        if (request.tipo() != null) {
            tipo = tipoTarjetaRepository.findByNombreIgnoreCase(request.tipo())
                    .orElseThrow(() -> {
                        log.warn("Rechazo de creación de tarjeta: tipo de tarjeta inexistente '{}'", request.tipo());
                        return new TipoTarjetaNoEncontradoException();
                    });
        }

        Tarjeta guardada = tarjetaRepository.save(new Tarjeta(request.nombrePropietario(), tipo));
        log.info("Tarjeta creada con id {}", guardada.getId());
        return toResponse(guardada);
    }

    public TarjetaResponse obtenerTarjeta(Long id) {
        Tarjeta tarjeta = tarjetaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Rechazo de consulta de tarjeta: id {} no encontrado", id);
                    return new TarjetaNoEncontradaException();
                });
        log.info("Tarjeta consultada con id {}", id);
        return toResponse(tarjeta);
    }

    private TarjetaResponse toResponse(Tarjeta tarjeta) {
        String tipo = tarjeta.getTipo() != null ? tarjeta.getTipo().getNombre() : null;
        return new TarjetaResponse(tarjeta.getId(), tarjeta.getNombrePropietario(), tipo);
    }
}