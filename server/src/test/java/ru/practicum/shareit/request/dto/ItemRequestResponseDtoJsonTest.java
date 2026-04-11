package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.item.ItemShortDTO;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestResponseDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestResponseDto> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        var dto = new ItemRequestResponseDto(
                1L,
                "Описание",
                10L,
                LocalDateTime.of(2025, 1, 1, 12, 0, 0),
                List.of(new ItemShortDTO(100L, "Вещь", 20L))
        );
        var json = jacksonTester.write(dto);
        assertThat(json).hasJsonPathNumberValue("$.id");
        assertThat(json).hasJsonPathStringValue("$.created");
        assertThat(json).extractingJsonPathStringValue("$.created").contains("2025-01-01T12:00:00");
    }

    @Test
    void testDeserialize() throws Exception {
        // CHECKSTYLE:OFF
        String content = """
                {
                    "id": 2,
                    "description": "Тест",
                    "requestorId": 5,
                    "created": "2025-01-02T10:00:00",
                    "items": []
                }
                """;
        // CHECKSTYLE:ON
        var dto = jacksonTester.parse(content).getObject();
        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.description()).isEqualTo("Тест");
        assertThat(dto.created()).isEqualTo(LocalDateTime.of(2025, 1, 2, 10, 0, 0));
    }
}