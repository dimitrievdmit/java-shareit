package ru.practicum.shareit.validator.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.validator.annotation.AtLeastOneField;
import ru.practicum.shareit.validator.implementation.typereference.MapTypeReference;

import java.util.Map;

public class AtLeastOneFieldValidator implements ConstraintValidator<AtLeastOneField, Object> {

    private final ObjectMapper objectMapper;

    @Autowired
    public AtLeastOneFieldValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isValid(Object dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        try {
            Map<String, Object> fieldMap = objectMapper.convertValue(dto, new MapTypeReference());
            return fieldMap.values().stream().anyMatch(this::isFieldFilled);
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isFieldFilled(Object value) {
        if (value == null) return false;
        if (value instanceof String str) return !str.isBlank();
        return true;
    }

}

