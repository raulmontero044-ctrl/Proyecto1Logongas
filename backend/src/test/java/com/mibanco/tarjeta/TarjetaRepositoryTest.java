package com.mibanco.tarjeta;

import com.mibanco.config.TarjetaDataInitializer;
import com.mibanco.tarjeta.entity.TipoTarjeta;
import com.mibanco.tarjeta.repository.TipoTarjetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TarjetaDataInitializer.class)
class TarjetaRepositoryTest {

    @Autowired
    private TipoTarjetaRepository tipoTarjetaRepository;

    @Autowired
    private TarjetaDataInitializer tarjetaDataInitializer;

    @BeforeEach
    void cleanCatalog() {
        tipoTarjetaRepository.deleteAll();
        tipoTarjetaRepository.flush();
    }

    @Test
    void buscarTipoPorNombreCoincideIgnorandoMayusculasYMinusculas() {
        tipoTarjetaRepository.save(new TipoTarjeta("Débito"));

        Optional<TipoTarjeta> enMinusculas = tipoTarjetaRepository.findByNombreIgnoreCase("débito");
        Optional<TipoTarjeta> enMayusculas = tipoTarjetaRepository.findByNombreIgnoreCase("DÉBITO");

        assertThat(enMinusculas).isPresent();
        assertThat(enMayusculas).isPresent();
        assertThat(enMinusculas.get().getId()).isEqualTo(enMayusculas.get().getId());
        assertThat(enMinusculas.get().getNombre()).isEqualTo("Débito");
    }

    @Test
    void findAllListaLosTiposSembradosConCapitalizacionCanonica() throws Exception {
        tarjetaDataInitializer.run();

        List<TipoTarjeta> tipos = tipoTarjetaRepository.findAll();

        assertThat(tipos)
                .extracting(TipoTarjeta::getNombre)
                .containsExactlyInAnyOrder("Débito", "Crédito");
    }

    @Test
    void seedPueblaElCatalogoSiEstaVacioYNoDuplicaSiYaHayDatos() throws Exception {
        tarjetaDataInitializer.run();
        tarjetaDataInitializer.run();

        assertThat(tipoTarjetaRepository.count()).isEqualTo(2);
        assertThat(tipoTarjetaRepository.findAll()).hasSize(2);
    }
}