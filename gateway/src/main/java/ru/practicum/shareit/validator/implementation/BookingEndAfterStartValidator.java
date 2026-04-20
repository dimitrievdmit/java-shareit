package ru.practicum.shareit.validator.implementation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.shareit.booking.dto.BookingCreateDTO;
import ru.practicum.shareit.validator.annotation.BookingEndAfterStart;

public class BookingEndAfterStartValidator implements ConstraintValidator<BookingEndAfterStart, BookingCreateDTO> {

    @Override
    public boolean isValid(BookingCreateDTO dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }
        if (dto.start() == null || dto.end() == null) {
            return true; // null проверяется @NotNull
        }
        if (dto.end().isAfter(dto.start())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Дата окончания бронирования должна быть позже даты начала")
                .addPropertyNode("end")
                .addConstraintViolation();
        return false;
    }
}