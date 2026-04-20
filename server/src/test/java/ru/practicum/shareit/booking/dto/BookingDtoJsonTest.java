package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.item.ItemResponseDTO;
import ru.practicum.shareit.user.dto.UserResponseDTO;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingCreateDTO> createTester;

    @Autowired
    private JacksonTester<BookingResponseDTO> responseTester;

    @Test
    void testBookingCreateDTO_Serialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 5, 10, 12, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 5, 15, 18, 0, 0);
        BookingCreateDTO dto = new BookingCreateDTO(1L, start, end);
        var json = createTester.write(dto);
        assertThat(json).hasJsonPathNumberValue("$.itemId");
        assertThat(json).extractingJsonPathStringValue("$.start").isEqualTo("2025-05-10T12:00:00");
        assertThat(json).extractingJsonPathStringValue("$.end").isEqualTo("2025-05-15T18:00:00");
    }

    @Test
    void testBookingCreateDTO_Deserialize() throws Exception {
        // CHECKSTYLE:OFF
        String content = """
                {
                    "itemId": 5,
                    "start": "2025-06-01T10:00:00",
                    "end": "2025-06-05T20:00:00"
                }
                """;
        // CHECKSTYLE:ON
        BookingCreateDTO dto = createTester.parse(content).getObject();
        assertThat(dto.itemId()).isEqualTo(5L);
        assertThat(dto.start()).isEqualTo(LocalDateTime.of(2025, 6, 1, 10, 0, 0));
        assertThat(dto.end()).isEqualTo(LocalDateTime.of(2025, 6, 5, 20, 0, 0));
    }

    @Test
    void testBookingResponseDTO_Serialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 7, 1, 9, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 7, 5, 17, 0, 0);
        ItemResponseDTO item = new ItemResponseDTO(10L, "Item", "Desc", true, null);
        UserResponseDTO user = new UserResponseDTO(20L, "User", "user@mail.com");
        BookingResponseDTO dto = new BookingResponseDTO(100L, start, end, item, user, BookingStatus.APPROVED);
        var json = responseTester.write(dto);
        assertThat(json).hasJsonPathNumberValue("$.id");
        assertThat(json).extractingJsonPathStringValue("$.start").isEqualTo("2025-07-01T09:00:00");
        assertThat(json).extractingJsonPathStringValue("$.end").isEqualTo("2025-07-05T17:00:00");
        assertThat(json).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
    }

    @Test
    void testBookingResponseDTO_Deserialize() throws Exception {
        // CHECKSTYLE:OFF
        String content = """
                {
                    "id": 200,
                    "start": "2025-08-01T14:00:00",
                    "end": "2025-08-10T12:00:00",
                    "item": {"id": 1, "name": "Item", "description": "Desc", "available": true},
                    "booker": {"id": 2, "name": "Booker", "email": "booker@mail.com"},
                    "status": "WAITING"
                }
                """;
        // CHECKSTYLE:ON
        BookingResponseDTO dto = responseTester.parse(content).getObject();
        assertThat(dto.id()).isEqualTo(200L);
        assertThat(dto.start()).isEqualTo(LocalDateTime.of(2025, 8, 1, 14, 0, 0));
        assertThat(dto.end()).isEqualTo(LocalDateTime.of(2025, 8, 10, 12, 0, 0));
        assertThat(dto.status()).isEqualTo(BookingStatus.WAITING);
    }
}