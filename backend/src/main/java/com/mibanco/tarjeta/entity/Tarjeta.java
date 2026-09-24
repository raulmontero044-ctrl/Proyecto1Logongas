package com.mibanco.tarjeta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tarjeta")
public class Tarjeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_propietario", length = 150, nullable = false)
    private String nombrePropietario;

    @ManyToOne
    @JoinColumn(name = "tipo_tarjeta_id", nullable = true)
    private TipoTarjeta tipo;

    protected Tarjeta() {
    }

    public Tarjeta(String nombrePropietario) {
        this.nombrePropietario = nombrePropietario;
    }

    public Tarjeta(String nombrePropietario, TipoTarjeta tipo) {
        this.nombrePropietario = nombrePropietario;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public String getNombrePropietario() {
        return nombrePropietario;
    }

    public void setNombrePropietario(String nombrePropietario) {
        this.nombrePropietario = nombrePropietario;
    }

    public TipoTarjeta getTipo() {
        return tipo;
    }

    public void setTipo(TipoTarjeta tipo) {
        this.tipo = tipo;
    }
}