package com.mibanco.config;

import com.mibanco.tarjeta.entity.TipoTarjeta;
import com.mibanco.tarjeta.repository.TipoTarjetaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TarjetaDataInitializer implements CommandLineRunner {

    private final TipoTarjetaRepository tipoTarjetaRepository;

    public TarjetaDataInitializer(TipoTarjetaRepository tipoTarjetaRepository) {
        this.tipoTarjetaRepository = tipoTarjetaRepository;
    }

    @Override
    public void run(String... args) {
        if (tipoTarjetaRepository.count() == 0) {
            tipoTarjetaRepository.saveAll(List.of(
                    new TipoTarjeta("Débito"),
                    new TipoTarjeta("Crédito")
            ));
        }
    }
}