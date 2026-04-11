package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestCreateDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestCreateDto> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Нужна вещь");

        JsonContent<ItemRequestCreateDto> json = jacksonTester.write(dto);

        assertThat(json).hasJsonPathStringValue("$.description");
        assertThat(json).extractingJsonPathStringValue("$.description").isEqualTo("Нужна вещь");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"description\": \"Нужна другая вещь\"}";
        ItemRequestCreateDto dto = jacksonTester.parse(content).getObject();

        assertThat(dto.description()).isEqualTo("Нужна другая вещь");
    }
}