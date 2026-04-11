package ru.practicum.shareit.validator.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.practicum.shareit.validator.implementation.AtLeastOneFieldValidator;

import java.lang.annotation.*;

@SuppressWarnings("unused")
@Documented
@Constraint(validatedBy = AtLeastOneFieldValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOneField {
    String message() default "Необходимо указать хотя бы одно поле для обновления";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
