package ru.practicum.shareit.booking.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateDTOJsonTest {

    @Autowired
    private JacksonTester<BookingCreateDTO> jacksonTester;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void testSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 5, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 5, 12, 18, 0);
        BookingCreateDTO dto = new BookingCreateDTO(1L, start, end);

        var json = jacksonTester.write(dto);

        assertThat(json).hasJsonPathNumberValue("$.itemId");
        assertThat(json).hasJsonPathStringValue("$.start");
        assertThat(json).hasJsonPathStringValue("$.end");
    }

    @Test
    void testDeserialize() throws Exception {
        // CHECKSTYLE:OFF
        String content = """
                {
                    "itemId": 5,
                    "start": "2025-06-01T12:00:00",
                    "end": "2025-06-05T12:00:00"
                }
                """;
        // CHECKSTYLE:ON
        BookingCreateDTO dto = jacksonTester.parse(content).getObject();

        assertThat(dto.itemId()).isEqualTo(5L);
        assertThat(dto.start()).isEqualTo(LocalDateTime.of(2025, 6, 1, 12, 0));
        assertThat(dto.end()).isEqualTo(LocalDateTime.of(2025, 6, 5, 12, 0));
    }

    @Test
    void shouldFailWhenEndBeforeStart() {
        LocalDateTime now = LocalDateTime.now();
        BookingCreateDTO dto = new BookingCreateDTO(1L, now.plusDays(2), now.plusDays(1));
        Set<ConstraintViolation<BookingCreateDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Дата окончания бронирования должна быть позже даты начала");
    }

    @Test
    void shouldPassWhenEndAfterStart() {
        LocalDateTime now = LocalDateTime.now();
        BookingCreateDTO dto = new BookingCreateDTO(1L, now.plusDays(1), now.plusDays(2));
        Set<ConstraintViolation<BookingCreateDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenStartEqualsEnd() {
        LocalDateTime now = LocalDateTime.now();
        BookingCreateDTO dto = new BookingCreateDTO(1L, now.plusDays(1), now.plusDays(1));
        Set<ConstraintViolation<BookingCreateDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Дата окончания бронирования должна быть позже даты начала");
    }
}