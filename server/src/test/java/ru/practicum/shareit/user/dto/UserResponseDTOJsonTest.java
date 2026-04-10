package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserResponseDTOJsonTest {

    @Autowired
    private JacksonTester<UserResponseDTO> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        UserResponseDTO dto = new UserResponseDTO(1L, "Test User", "test@mail.com");
        var json = jacksonTester.write(dto);
        assertThat(json).hasJsonPathNumberValue("$.id");
        assertThat(json).hasJsonPathStringValue("$.name");
        assertThat(json).hasJsonPathStringValue("$.email");
        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Test User");
        assertThat(json).extractingJsonPathStringValue("$.email").isEqualTo("test@mail.com");
    }

    @Test
    void testDeserialize() throws Exception {
        // CHECKSTYLE:OFF
        String content = """
                {
                    "id": 2,
                    "name": "John Doe",
                    "email": "john@example.com"
                }
                """;
        // CHECKSTYLE:ON
        UserResponseDTO dto = jacksonTester.parse(content).getObject();
        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.name()).isEqualTo("John Doe");
        assertThat(dto.email()).isEqualTo("john@example.com");
    }
}