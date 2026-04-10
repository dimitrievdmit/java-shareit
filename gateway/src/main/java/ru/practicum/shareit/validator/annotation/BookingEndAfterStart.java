package ru.practicum.shareit.validator.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.practicum.shareit.validator.implementation.BookingEndAfterStartValidator;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BookingEndAfterStartValidator.class)
@Documented
public @interface BookingEndAfterStart {
    String message() default "Дата окончания бронирования должна быть позже даты начала";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}