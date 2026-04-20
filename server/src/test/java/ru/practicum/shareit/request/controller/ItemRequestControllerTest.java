package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.item.ItemShortDTO;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    private ItemRequestService itemRequestService;

    private ItemRequestResponseDto responseDto;
    private ItemRequestCreateDto createDto;

    @BeforeEach
    void setUp() {
        createDto = new ItemRequestCreateDto("Нужна дрель");
        responseDto = new ItemRequestResponseDto(
                1L,
                "Нужна дрель",
                10L,
                LocalDateTime.now(),
                List.of(new ItemShortDTO(100L, "Дрель", 20L))
        );
    }

    // --- POST /requests ---
    @Test
    void create_Success() throws Exception {
        when(itemRequestService.create(any(ItemRequestCreateDto.class), eq(1L))).thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Нужна дрель"))
                .andExpect(jsonPath("$.requestorId").value(10L))
                .andExpect(jsonPath("$.items[0].id").value(100L));

        verify(itemRequestService).create(any(ItemRequestCreateDto.class), eq(1L));
    }

    @Test
    void create_UserNotFound_ShouldReturn404() throws Exception {
        when(itemRequestService.create(any(ItemRequestCreateDto.class), eq(999L)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound());
    }

    // --- GET /requests ---
    @Test
    void getUserRequests_Success() throws Exception {
        List<ItemRequestResponseDto> requests = List.of(responseDto);
        when(itemRequestService.getUserRequests(1L)).thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"));

        verify(itemRequestService).getUserRequests(1L);
    }

    // --- GET /requests/all ---
    @Test
    void getNonUserRequests_Success() throws Exception {
        List<ItemRequestResponseDto> requests = List.of(responseDto);
        when(itemRequestService.getNonUserRequests(eq(0), eq(10), eq(1L))).thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getNonUserRequests_DefaultPagination() throws Exception {
        when(itemRequestService.getNonUserRequests(eq(0), eq(10), eq(1L))).thenReturn(List.of());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    // --- GET /requests/{requestId} ---
    @Test
    void getById_Success() throws Exception {
        when(itemRequestService.getById(eq(1L), eq(1L))).thenReturn(responseDto);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getById_RequestNotFound_ShouldReturn404() throws Exception {
        when(itemRequestService.getById(eq(999L), eq(1L)))
                .thenThrow(new NotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }
}