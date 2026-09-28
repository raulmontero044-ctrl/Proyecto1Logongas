package com.mibanco.common.dispatcher;

import com.mibanco.common.dispatcher.exception.CuerpoInvalidoException;
import com.mibanco.common.dispatcher.exception.SolicitudInvalidaException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

@Component
public class DeserializadorCuerpo {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public DeserializadorCuerpo(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public <T> T deserializarYValidar(String cuerpo, Class<T> tipo) {
        T instancia = leer(cuerpo, tipo);
        Set<ConstraintViolation<T>> violaciones = validator.validate(instancia);
        if (!violaciones.isEmpty()) {
            ConstraintViolation<T> primera = violaciones.iterator().next();
            throw new SolicitudInvalidaException(primera.getPropertyPath().toString(), primera.getMessage());
        }
        return instancia;
    }

    private <T> T leer(String cuerpo, Class<T> tipo) {
        if (cuerpo == null || cuerpo.isBlank()) {
            throw new CuerpoInvalidoException();
        }
        try {
            return objectMapper.readValue(cuerpo, tipo);
        } catch (JacksonException e) {
            throw new CuerpoInvalidoException();
        }
    }
}
