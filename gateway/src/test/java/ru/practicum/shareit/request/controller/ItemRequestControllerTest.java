package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.validator.Validator;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    private ItemRequestCreateDto validCreateDto;

    @BeforeEach
    void setUp() {
        validCreateDto = new ItemRequestCreateDto("Нужна дрель для ремонта");
    }

    @Test
    void create_withValidData_shouldCallClient() throws Exception {
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isCreated());

        verify(itemRequestClient).create(eq(1L), any(ItemRequestCreateDto.class));
    }

    @Test
    void create_withBlankDescription_shouldReturnBadRequest() throws Exception {
        ItemRequestCreateDto invalid = new ItemRequestCreateDto("");
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getUserRequests_withValidUserId_shouldCallClient() throws Exception {
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", "2"))
                .andExpect(status().isOk());

        verify(itemRequestClient).getUserRequests(2L);
    }

    @Test
    void getNonUserRequests_withValidPagination_shouldCallClient() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "3")
                        .param("from", "0")
                        .param("size", "15"))
                .andExpect(status().isOk());

        verify(itemRequestClient).getNonUserRequests(0, 15, 3L);
    }

    @Test
    void getNonUserRequests_withNegativeFrom_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "3")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getNonUserRequests_withZeroSize_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "3")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getNonUserRequests_withTooLargeSize_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "3")
                        .param("from", "0")
                        .param("size", "1000"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getById_withValidIds_shouldCallClient() throws Exception {
        mockMvc.perform(get("/requests/42")
                        .header("X-Sharer-User-Id", "4"))
                .andExpect(status().isOk());

        verify(itemRequestClient).getById(4L, 42L);
    }

    // ========== Валидация ItemRequestCreateDto ==========

    @Test
    void create_withDescriptionTooLong_shouldReturnBadRequest() throws Exception {
        String longDescription = "a".repeat(Validator.MAX_DESCRIPTION_LENGTH + 1);
        ItemRequestCreateDto invalidDto = new ItemRequestCreateDto(longDescription);
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Описание не может быть длиннее")));
        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void create_withDescriptionOnlySpaces_shouldReturnBadRequest() throws Exception {
        ItemRequestCreateDto invalidDto = new ItemRequestCreateDto("   ");
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Описание не может быть пустым")));
        verifyNoInteractions(itemRequestClient);
    }
}