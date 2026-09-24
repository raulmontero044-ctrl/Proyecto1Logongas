package com.mibanco.tarjeta.controller;

import com.mibanco.tarjeta.dto.TarjetaRequest;
import com.mibanco.tarjeta.dto.TarjetaResponse;
import com.mibanco.tarjeta.dto.TipoTarjetaResponse;
import com.mibanco.tarjeta.service.TarjetaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TarjetaController {

    private final TarjetaService tarjetaService;

    public TarjetaController(TarjetaService tarjetaService) {
        this.tarjetaService = tarjetaService;
    }

    @PostMapping("/tarjetas")
    @ResponseStatus(HttpStatus.CREATED)
    public TarjetaResponse crearTarjeta(@Valid @RequestBody TarjetaRequest request) {
        return tarjetaService.crearTarjeta(request);
    }

    @GetMapping("/tarjetas/{id}")
    public TarjetaResponse obtenerTarjeta(@PathVariable Long id) {
        return tarjetaService.obtenerTarjeta(id);
    }

    @GetMapping("/tipos-tarjetas")
    public List<TipoTarjetaResponse> listarTiposTarjeta() {
        return tarjetaService.listarTiposTarjeta();
    }
}