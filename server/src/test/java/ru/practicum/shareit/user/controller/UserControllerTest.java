package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDTO;
import ru.practicum.shareit.user.dto.UserResponseDTO;
import ru.practicum.shareit.user.dto.UserUpdateDTO;
import ru.practicum.shareit.user.service.UserService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private final UserService userService;

    private final ObjectMapper objectMapper;

    private final UserResponseDTO testUser = new UserResponseDTO(1L, "Test User", "test@yandex.ru");
    private final UserCreateDTO createDTO = new UserCreateDTO("Test User", "test@yandex.ru");
    private final UserUpdateDTO updateDTO = new UserUpdateDTO("Updated Name", "new@yandex.ru");

    // --- Тесты для POST /users ---

    @Test
    void createUser_ValidData_ShouldReturnCreated() throws Exception {
        when(userService.create(any(UserCreateDTO.class))).thenReturn(testUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@yandex.ru"));

        verify(userService, times(1)).create(any(UserCreateDTO.class));
    }

    @Test
    void createUser_EmailAlreadyExists_ShouldReturnConflict() throws Exception {
        when(userService.create(any(UserCreateDTO.class)))
                .thenThrow(new AlreadyExistException("Пользователь с email 'test@yandex.ru' уже существует"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ошибка конфликта"))
                .andExpect(jsonPath("$.description").value("Пользователь с email 'test@yandex.ru' уже существует"));
    }

    // --- Тесты для GET /users/{id} ---

    @Test
    void getUser_ValidId_ShouldReturnUser() throws Exception {
        when(userService.get(1L)).thenReturn(testUser);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@yandex.ru"));

        verify(userService, times(1)).get(1L);
    }

    @Test
    void getUser_NonExistentId_ShouldReturnNotFound() throws Exception {
        when(userService.get(999L)).thenThrow(new NotFoundException("Пользователь с id = 999 не найден"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Не найдено"))
                .andExpect(jsonPath("$.description").value("Пользователь с id = 999 не найден"));
    }

    // --- Тесты для PATCH /users/{id} ---

    @Test
    void updateUser_ValidData_ShouldReturnUpdatedUser() throws Exception {
        when(userService.update(1L, updateDTO)).thenReturn(
                new UserResponseDTO(1L, "Updated Name", "new@yandex.ru"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("new@yandex.ru"));

        verify(userService, times(1)).update(1L, updateDTO);
    }

    @Test
    void updateUser_NonExistentId_ShouldReturnNotFound() throws Exception {
        when(userService.update(999L, updateDTO))
                .thenThrow(new NotFoundException("Пользователь с id = 999 не найден"));

        mockMvc.perform(patch("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Не найдено"))
                .andExpect(jsonPath("$.description").value("Пользователь с id = 999 не найден"));
    }

    @Test
    void updateUser_EmailAlreadyExists_ShouldReturnConflict() throws Exception {
        when(userService.update(1L, updateDTO))
                .thenThrow(new AlreadyExistException("Пользователь с email 'new@yandex.ru' уже существует"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ошибка конфликта"))
                .andExpect(jsonPath("$.description").value("Пользователь с email 'new@yandex.ru' уже существует"));
    }

    @Test
    void updateUser_PartialUpdateWithOnlyName_ShouldReturnUpdatedUser() throws Exception {
        UserUpdateDTO partialUpdate = new UserUpdateDTO("New Name", null);
        UserResponseDTO expectedResponse = new UserResponseDTO(1L, "New Name", "test@yandex.ru");

        when(userService.update(1L, partialUpdate)).thenReturn(expectedResponse);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.email").value("test@yandex.ru"));

        verify(userService, times(1)).update(1L, partialUpdate);
    }

    @Test
    void updateUser_PartialUpdateWithOnlyEmail_ShouldReturnUpdatedUser() throws Exception {
        UserUpdateDTO partialUpdate = new UserUpdateDTO(null, "newemail@yandex.ru");
        UserResponseDTO expectedResponse = new UserResponseDTO(1L, "Test User", "newemail@yandex.ru");

        when(userService.update(1L, partialUpdate)).thenReturn(expectedResponse);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("newemail@yandex.ru"));

        verify(userService, times(1)).update(1L, partialUpdate);
    }

    // --- Тесты для DELETE /users/{id} ---

    @Test
    void deleteUser_ValidId_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).delete(1L);
    }

    @Test
    void deleteUser_NonExistentId_ShouldReturnNotFound() throws Exception {
        doThrow(new NotFoundException("Пользователь с id = 999 не найден"))
                .when(userService).delete(999L);

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Не найдено"))
                .andExpect(jsonPath("$.description").value("Пользователь с id = 999 не найден"));
    }
}