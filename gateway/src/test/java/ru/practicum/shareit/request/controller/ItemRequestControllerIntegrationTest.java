package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.controller.GatewayIntegrationTestBase;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemRequestControllerIntegrationTest extends GatewayIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_validRequest_shouldReturnClientResponse() throws Exception {
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto("Нужна дрель");
        ResponseEntity<Object> clientResponse = ResponseEntity.status(HttpStatus.CREATED)
                .body("{\"id\":1,\"description\":\"Нужна дрель\",\"requestorId\":1}");

        when(itemRequestClient.create(eq(1L), any(ItemRequestCreateDto.class))).thenReturn(clientResponse);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"id\":1,\"description\":\"Нужна дрель\"}"));
    }

    @Test
    void getUserRequests_validRequest_shouldReturnClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.ok("[{\"id\":10,\"description\":\"Книга\"}]");
        when(itemRequestClient.getUserRequests(2L)).thenReturn(clientResponse);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":10,\"description\":\"Книга\"}]"));
    }

    @Test
    void getNonUserRequests_validRequest_shouldReturnClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.ok("[{\"id\":20},{\"id\":21}]");
        when(itemRequestClient.getNonUserRequests(0, 15, 3L)).thenReturn(clientResponse);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "3")
                        .param("from", "0")
                        .param("size", "15"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":20},{\"id\":21}]"));
    }

    @Test
    void getById_validRequest_shouldReturnClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.ok("{\"id\":30,\"description\":\"Стул\"}");
        when(itemRequestClient.getById(4L, 30L)).thenReturn(clientResponse);

        mockMvc.perform(get("/requests/30")
                        .header("X-Sharer-User-Id", "4"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":30,\"description\":\"Стул\"}"));
    }
}