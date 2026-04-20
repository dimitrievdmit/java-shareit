package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserCreateDTO;
import ru.practicum.shareit.user.dto.UserUpdateDTO;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    private UserCreateDTO validCreateDto;
    private UserUpdateDTO validUpdateDto;

    @BeforeEach
    void setUp() {
        validCreateDto = new UserCreateDTO("John", "john@example.com");
        validUpdateDto = new UserUpdateDTO("John Updated", "john.new@example.com");
    }

    @Test
    void create_withValidData_shouldCallClient() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isCreated());

        verify(userClient).create(any(UserCreateDTO.class));
    }

    @Test
    void create_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        UserCreateDTO invalid = new UserCreateDTO("not-an-email", "John");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void get_withValidId_shouldCallClient() throws Exception {
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());

        verify(userClient).get(1L);
    }

    @Test
    void get_withZeroId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/users/0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void update_withValidData_shouldCallClient() throws Exception {
        mockMvc.perform(patch("/users/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateDto)))
                .andExpect(status().isOk());

        verify(userClient).update(eq(5L), any(UserUpdateDTO.class));
    }

    @Test
    void delete_withValidId_shouldCallClient() throws Exception {
        mockMvc.perform(delete("/users/10"))
                .andExpect(status().isNoContent());

        verify(userClient).delete(10L);
    }

// ========== Частичные обновления ==========

    @Test
    void updateUser_PartialUpdateOnlyName() throws Exception {
        UserUpdateDTO partial = new UserUpdateDTO("New Name", null);
        ResponseEntity<Object> clientResponse = ResponseEntity.ok("{\"id\":1,\"name\":\"New Name\",\"email\":\"old@mail.com\"}");
        when(userClient.update(1L, partial)).thenReturn(clientResponse);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partial)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.email").value("old@mail.com"));
    }

    @Test
    void updateUser_PartialUpdateOnlyEmail() throws Exception {
        UserUpdateDTO partial = new UserUpdateDTO(null, "new@mail.com");
        ResponseEntity<Object> clientResponse = ResponseEntity.ok("{\"id\":1,\"name\":\"Old Name\",\"email\":\"new@mail.com\"}");
        when(userClient.update(1L, partial)).thenReturn(clientResponse);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partial)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Old Name"))
                .andExpect(jsonPath("$.email").value("new@mail.com"));
    }

// ========== Проксирование ошибок от клиента ==========

    @Test
    void getUser_NotFound_ShouldProxyClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\":\"User not found\"}");
        when(userClient.get(999L)).thenReturn(clientResponse);

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"User not found\"}"));
    }

    @Test
    void updateUser_NotFound_ShouldProxyClientResponse() throws Exception {
        UserUpdateDTO update = new UserUpdateDTO("Name", null);
        ResponseEntity<Object> clientResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        when(userClient.update(999L, update)).thenReturn(clientResponse);

        mockMvc.perform(patch("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_NotFound_ShouldProxyClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        when(userClient.delete(999L)).thenReturn(clientResponse);

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_Conflict_ShouldProxyClientResponse() throws Exception {
        UserUpdateDTO update = new UserUpdateDTO(null, "existing@mail.com");
        ResponseEntity<Object> clientResponse = ResponseEntity.status(HttpStatus.CONFLICT)
                .body("{\"error\":\"Тест корректной передачи сообщения об ошибке из клиента контроллером\"}");
        when(userClient.update(1L, update)).thenReturn(clientResponse);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isConflict())
                .andExpect(content().json("{\"error\":\"Тест корректной передачи сообщения об ошибке из клиента контроллером\"}"));
    }
}