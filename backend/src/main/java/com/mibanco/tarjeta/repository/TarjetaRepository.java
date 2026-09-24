package com.mibanco.tarjeta.repository;

import com.mibanco.tarjeta.entity.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarjetaRepository extends JpaRepository<Tarjeta, Long> {
}