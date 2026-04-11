package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.controller.GatewayIntegrationTestBase;
import ru.practicum.shareit.user.dto.UserCreateDTO;
import ru.practicum.shareit.user.dto.UserUpdateDTO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest extends GatewayIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_validRequest_shouldReturnClientResponse() throws Exception {
        UserCreateDTO requestDto = new UserCreateDTO("User", "user@example.com");
        ResponseEntity<Object> clientResponse = ResponseEntity.status(HttpStatus.CREATED)
                .body("{\"id\":1,\"email\":\"user@example.com\",\"name\":\"User\"}");

        when(userClient.create(any(UserCreateDTO.class))).thenReturn(clientResponse);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"id\":1,\"email\":\"user@example.com\",\"name\":\"User\"}"));
    }

    @Test
    void get_validRequest_shouldReturnClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.ok("{\"id\":2,\"name\":\"John\"}");
        when(userClient.get(2L)).thenReturn(clientResponse);

        mockMvc.perform(get("/users/2"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":2,\"name\":\"John\"}"));
    }

    @Test
    void update_validRequest_shouldReturnClientResponse() throws Exception {
        UserUpdateDTO updateDto = new UserUpdateDTO("NewName", "new@example.com");
        ResponseEntity<Object> clientResponse = ResponseEntity.ok("{\"id\":3,\"email\":\"new@example.com\",\"name\":\"NewName\"}");
        when(userClient.update(eq(3L), any(UserUpdateDTO.class))).thenReturn(clientResponse);

        mockMvc.perform(patch("/users/3")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":3,\"email\":\"new@example.com\",\"name\":\"NewName\"}"));
    }

    @Test
    void delete_validRequest_shouldReturnClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.noContent().build();
        when(userClient.delete(4L)).thenReturn(clientResponse);

        mockMvc.perform(delete("/users/4"))
                .andExpect(status().isNoContent());
    }
}