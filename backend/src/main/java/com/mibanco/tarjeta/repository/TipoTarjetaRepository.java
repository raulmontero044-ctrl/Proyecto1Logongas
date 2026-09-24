package com.mibanco.tarjeta.repository;

import com.mibanco.tarjeta.entity.TipoTarjeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoTarjetaRepository extends JpaRepository<TipoTarjeta, Long> {

    Optional<TipoTarjeta> findByNombreIgnoreCase(String nombre);
}