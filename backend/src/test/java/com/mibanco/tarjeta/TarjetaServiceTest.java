package com.mibanco.tarjeta;

import com.mibanco.tarjeta.dto.TarjetaRequest;
import com.mibanco.tarjeta.dto.TarjetaResponse;
import com.mibanco.tarjeta.entity.Tarjeta;
import com.mibanco.tarjeta.entity.TipoTarjeta;
import com.mibanco.tarjeta.exception.NombrePropietarioRequeridoException;
import com.mibanco.tarjeta.exception.TarjetaNoEncontradaException;
import com.mibanco.tarjeta.exception.TipoTarjetaNoEncontradoException;
import com.mibanco.tarjeta.repository.TarjetaRepository;
import com.mibanco.tarjeta.repository.TipoTarjetaRepository;
import com.mibanco.tarjeta.service.TarjetaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarjetaServiceTest {

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private TipoTarjetaRepository tipoTarjetaRepository;

    private TarjetaService tarjetaService;

    @BeforeEach
    void setUp() {
        tarjetaService = new TarjetaService(tarjetaRepository, tipoTarjetaRepository);
    }

    @Test
    void crearTarjetaConNombreYTipoValidosLaPersisteConIdUnicoAsignado() {
        TipoTarjeta debito = new TipoTarjeta("Débito");
        when(tipoTarjetaRepository.findByNombreIgnoreCase("Débito")).thenReturn(Optional.of(debito));
        when(tarjetaRepository.save(any(Tarjeta.class))).thenAnswer(invocation -> {
            Tarjeta guardada = invocation.getArgument(0);
            ReflectionTestUtils.setField(guardada, "id", 10L);
            return guardada;
        });

        TarjetaResponse respuesta = tarjetaService.crearTarjeta(new TarjetaRequest("María López", "Débito"));

        ArgumentCaptor<Tarjeta> captor = ArgumentCaptor.forClass(Tarjeta.class);
        verify(tarjetaRepository).save(captor.capture());
        assertThat(captor.getValue().getNombrePropietario()).isEqualTo("María López");
        assertThat(captor.getValue().getTipo()).isSameAs(debito);
        assertThat(respuesta.id()).isEqualTo(10L);
        assertThat(respuesta.nombrePropietario()).isEqualTo("María López");
        assertThat(respuesta.tipo()).isEqualTo("Débito");
    }

    @Test
    void crearTarjetaConNombreDelPropietarioVacioEsRechazada() {
        assertThatThrownBy(() -> tarjetaService.crearTarjeta(new TarjetaRequest("   ", "Débito")))
                .isInstanceOf(NombrePropietarioRequeridoException.class)
                .hasMessage("El nombre del propietario es obligatorio");

        verify(tarjetaRepository, never()).save(any());
    }

    @Test
    void crearTarjetaResuelveElTipoIgnorandoMayusculasYMinusculas() {
        TipoTarjeta debito = new TipoTarjeta("Débito");
        when(tipoTarjetaRepository.findByNombreIgnoreCase("débito")).thenReturn(Optional.of(debito));
        when(tipoTarjetaRepository.findByNombreIgnoreCase("DÉBITO")).thenReturn(Optional.of(debito));
        when(tarjetaRepository.save(any(Tarjeta.class))).thenAnswer(invocation -> {
            Tarjeta guardada = invocation.getArgument(0);
            ReflectionTestUtils.setField(guardada, "id", 11L);
            return guardada;
        });

        TarjetaResponse conMinusculas = tarjetaService.crearTarjeta(new TarjetaRequest("Ana Pérez", "débito"));
        TarjetaResponse conMayusculas = tarjetaService.crearTarjeta(new TarjetaRequest("Luis Gómez", "DÉBITO"));

        verify(tipoTarjetaRepository).findByNombreIgnoreCase("débito");
        verify(tipoTarjetaRepository).findByNombreIgnoreCase("DÉBITO");
        ArgumentCaptor<Tarjeta> captor = ArgumentCaptor.forClass(Tarjeta.class);
        verify(tarjetaRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(tarjeta -> assertThat(tarjeta.getTipo()).isSameAs(debito));
        assertThat(conMinusculas.tipo()).isEqualTo("Débito");
        assertThat(conMayusculas.tipo()).isEqualTo("Débito");
    }

    @Test
    void crearTarjetaConTipoInexistenteEsRechazada() {
        when(tipoTarjetaRepository.findByNombreIgnoreCase("Bienvenida")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tarjetaService.crearTarjeta(new TarjetaRequest("Ana Pérez", "Bienvenida")))
                .isInstanceOf(TipoTarjetaNoEncontradoException.class)
                .hasMessage("El tipo de tarjeta no existe");

        verify(tarjetaRepository, never()).save(any());
    }

    @Test
    void crearTarjetaSinTipoEsPermitida() {
        when(tarjetaRepository.save(any(Tarjeta.class))).thenAnswer(invocation -> {
            Tarjeta guardada = invocation.getArgument(0);
            ReflectionTestUtils.setField(guardada, "id", 12L);
            return guardada;
        });

        TarjetaResponse respuesta = tarjetaService.crearTarjeta(new TarjetaRequest("Carlos Ruiz", null));

        verify(tipoTarjetaRepository, never()).findByNombreIgnoreCase(anyString());
        ArgumentCaptor<Tarjeta> captor = ArgumentCaptor.forClass(Tarjeta.class);
        verify(tarjetaRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isNull();
        assertThat(respuesta.id()).isEqualTo(12L);
        assertThat(respuesta.nombrePropietario()).isEqualTo("Carlos Ruiz");
        assertThat(respuesta.tipo()).isNull();
    }

    @Test
    void crearTarjetaPropagaLaViolacionDeIntegridadPorIdentificadorDuplicado() {
        when(tarjetaRepository.save(any(Tarjeta.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry for primary key"));

        assertThatThrownBy(() -> tarjetaService.crearTarjeta(new TarjetaRequest("Ana Pérez", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void obtenerTarjetaExistenteDevuelveLaRespuestaConElTipoCanonico() {
        TipoTarjeta credito = new TipoTarjeta("Crédito");
        Tarjeta tarjeta = new Tarjeta("María López", credito);
        ReflectionTestUtils.setField(tarjeta, "id", 7L);
        when(tarjetaRepository.findById(7L)).thenReturn(Optional.of(tarjeta));

        TarjetaResponse respuesta = tarjetaService.obtenerTarjeta(7L);

        assertThat(respuesta.id()).isEqualTo(7L);
        assertThat(respuesta.nombrePropietario()).isEqualTo("María López");
        assertThat(respuesta.tipo()).isEqualTo("Crédito");
    }

    @Test
    void obtenerTarjetaInexistenteComunicaQueNoExiste() {
        when(tarjetaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tarjetaService.obtenerTarjeta(99L))
                .isInstanceOf(TarjetaNoEncontradaException.class)
                .hasMessage("Tarjeta no encontrada");
    }
}