package com.mibanco.tarjeta;

import com.mibanco.config.TarjetaDataInitializer;
import com.mibanco.tarjeta.entity.Tarjeta;
import com.mibanco.tarjeta.entity.TipoTarjeta;
import com.mibanco.tarjeta.repository.TarjetaRepository;
import com.mibanco.tarjeta.repository.TipoTarjetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TarjetaDataInitializer.class)
class TarjetaRepositoryTest {

    @Autowired
    private TipoTarjetaRepository tipoTarjetaRepository;

    @Autowired
    private TarjetaRepository tarjetaRepository;

    @Autowired
    private TarjetaDataInitializer tarjetaDataInitializer;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        tarjetaRepository.deleteAll();
        tarjetaRepository.flush();
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

    @Test
    void guardarYRecuperarTarjetaSinTipoAsignaIdYGuardaNombreDePropietario() {
        Tarjeta tarjeta = tarjetaRepository.save(new Tarjeta("María López"));

        Optional<Tarjeta> recuperada = tarjetaRepository.findById(tarjeta.getId());

        assertThat(recuperada).isPresent();
        assertThat(recuperada.get().getId()).isNotNull();
        assertThat(recuperada.get().getNombrePropietario()).isEqualTo("María López");
        assertThat(recuperada.get().getTipo()).isNull();
    }

    @Test
    void guardarYRecuperarTarjetaConTipoReferenciadoAlCatalogo() {
        TipoTarjeta tipo = tipoTarjetaRepository.save(new TipoTarjeta("Débito"));

        Tarjeta tarjeta = tarjetaRepository.save(new Tarjeta("Carlos Ruiz", tipo));

        Optional<Tarjeta> recuperada = tarjetaRepository.findById(tarjeta.getId());

        assertThat(recuperada).isPresent();
        assertThat(recuperada.get().getTipo()).isNotNull();
        assertThat(recuperada.get().getTipo().getId()).isEqualTo(tipo.getId());
        assertThat(recuperada.get().getTipo().getNombre()).isEqualTo("Débito");
    }

    @Test
    void guardarTarjetaConIdentificadorDuplicadoEsRechazadoPorConstraintDeClavePrimaria() {
        Tarjeta primera = tarjetaRepository.save(new Tarjeta("Ana Pérez"));
        Long idExistente = primera.getId();
        assertThat(idExistente).isNotNull();

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO tarjeta (id, nombre_propietario, tipo_tarjeta_id) VALUES (?, ?, NULL)",
                idExistente, "Luis Gómez"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
