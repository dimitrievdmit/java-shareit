package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.controller.GatewayIntegrationTestBase;
import ru.practicum.shareit.item.dto.comment.CommentCreateDTO;
import ru.practicum.shareit.item.dto.item.ItemCreateDTO;
import ru.practicum.shareit.item.dto.item.ItemUpdateDTO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerIntegrationTest extends GatewayIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_validRequest_shouldReturnClientResponse() throws Exception {
        ItemCreateDTO requestDto = new ItemCreateDTO("Дрель", "Аккумуляторная", true, null);
        ResponseEntity<Object> clientResponse = ResponseEntity.status(HttpStatus.CREATED)
                .body("{\"id\":100,\"name\":\"Дрель\"}");

        when(itemClient.create(eq(1L), any(ItemCreateDTO.class))).thenReturn(clientResponse);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"id\":100,\"name\":\"Дрель\"}"));
    }

    @Test
    void get_validRequest_shouldReturnClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.ok("{\"id\":200,\"name\":\"Отвёртка\"}");
        when(itemClient.get(2L, 200L)).thenReturn(clientResponse);

        mockMvc.perform(get("/items/200")
                        .header("X-Sharer-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":200,\"name\":\"Отвёртка\"}"));
    }

    @Test
    void search_validRequest_shouldReturnClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.ok("[{\"id\":300},{\"id\":301}]");
        when(itemClient.search("дрель")).thenReturn(clientResponse);

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":300},{\"id\":301}]"));
    }

    @Test
    void update_validRequest_shouldReturnClientResponse() throws Exception {
        ItemUpdateDTO updateDto = new ItemUpdateDTO("Новое имя", null, null, null);
        ResponseEntity<Object> clientResponse = ResponseEntity.ok("{\"id\":400,\"name\":\"Новое имя\"}");
        when(itemClient.update(eq(3L), eq(400L), any(ItemUpdateDTO.class))).thenReturn(clientResponse);

        mockMvc.perform(patch("/items/400")
                        .header("X-Sharer-User-Id", "3")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":400,\"name\":\"Новое имя\"}"));
    }

    @Test
    void delete_validRequest_shouldReturnClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.noContent().build();
        when(itemClient.delete(4L, 500L)).thenReturn(clientResponse);

        mockMvc.perform(delete("/items/500")
                        .header("X-Sharer-User-Id", "4"))
                .andExpect(status().isNoContent());
    }

    @Test
    void createComment_validRequest_shouldReturnClientResponse() throws Exception {
        CommentCreateDTO commentDto = new CommentCreateDTO("Отличная вещь");
        ResponseEntity<Object> clientResponse = ResponseEntity.status(HttpStatus.CREATED)
                .body("{\"id\":10,\"text\":\"Отличная вещь\"}");

        when(itemClient.createComment(eq(5L), eq(600L), any(CommentCreateDTO.class))).thenReturn(clientResponse);

        mockMvc.perform(post("/items/600/comment")
                        .header("X-Sharer-User-Id", "5")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"id\":10,\"text\":\"Отличная вещь\"}"));
    }
}