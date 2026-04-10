package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.comment.CommentCreateDTO;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentCreateDTOJsonTest {

    @Autowired
    private JacksonTester<CommentCreateDTO> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        CommentCreateDTO dto = new CommentCreateDTO("Хорошая вещь");

        var json = jacksonTester.write(dto);

        assertThat(json).hasJsonPathStringValue("$.text");
        assertThat(json).extractingJsonPathStringValue("$.text").isEqualTo("Хорошая вещь");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"text\": \"Отлично!\"}";
        CommentCreateDTO dto = jacksonTester.parse(content).getObject();

        assertThat(dto.text()).isEqualTo("Отлично!");
    }
}