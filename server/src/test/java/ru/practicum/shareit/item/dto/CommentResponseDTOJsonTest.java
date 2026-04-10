package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.comment.CommentResponseDTO;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentResponseDTOJsonTest {

    @Autowired
    private JacksonTester<CommentResponseDTO> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        LocalDateTime created = LocalDateTime.of(2025, 4, 10, 15, 30, 0);
        CommentResponseDTO dto = new CommentResponseDTO(1L, "Отлично!", "Иван", created);
        var json = jacksonTester.write(dto);
        assertThat(json).hasJsonPathNumberValue("$.id");
        assertThat(json).hasJsonPathStringValue("$.text");
        assertThat(json).hasJsonPathStringValue("$.authorName");
        assertThat(json).extractingJsonPathStringValue("$.created").isEqualTo("2025-04-10T15:30:00");
    }

    @Test
    void testDeserialize() throws Exception {
        // CHECKSTYLE:OFF
        String content = """
                {
                    "id": 2,
                    "text": "Хорошо",
                    "authorName": "Петр",
                    "created": "2025-04-11T10:00:00"
                }
                """;
        // CHECKSTYLE:ON
        CommentResponseDTO dto = jacksonTester.parse(content).getObject();
        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.text()).isEqualTo("Хорошо");
        assertThat(dto.authorName()).isEqualTo("Петр");
        assertThat(dto.created()).isEqualTo(LocalDateTime.of(2025, 4, 11, 10, 0, 0));
    }
}