package com.mibanco.tarjeta.dto;

import jakarta.validation.constraints.NotBlank;

public record TarjetaRequest(
        @NotBlank(message = "El nombre del propietario es obligatorio")
        String nombrePropietario,
        String tipo) {
}