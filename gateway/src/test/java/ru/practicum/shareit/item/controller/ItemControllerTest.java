package ru.practicum.shareit.item.controller;

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
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.comment.CommentCreateDTO;
import ru.practicum.shareit.item.dto.item.ItemCreateDTO;
import ru.practicum.shareit.item.dto.item.ItemUpdateDTO;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    private ItemCreateDTO validItemCreate;
    private ItemUpdateDTO validItemUpdate;
    private CommentCreateDTO validComment;

    @BeforeEach
    void setUp() {
        validItemCreate = new ItemCreateDTO(
                "Дрель",
                "Аккумуляторная дрель",
                true,
                null
        );

        validItemUpdate = new ItemUpdateDTO(
                "Дрель обновлённая",
                "Описание обновлённое",
                false,
                null
        );

        validComment = new CommentCreateDTO("Отличная вещь!");
    }

    @Test
    void create_withValidData_shouldCallClient() throws Exception {
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemCreate)))
                .andExpect(status().isCreated());

        verify(itemClient).create(eq(1L), any(ItemCreateDTO.class));
    }

    @Test
    void create_withMissingUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemCreate)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void create_withInvalidUserIdZero_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemCreate)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void create_withInvalidItemNameBlank_shouldReturnBadRequest() throws Exception {
        ItemCreateDTO invalid = new ItemCreateDTO("", "desc", true, null);
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void get_withValidIds_shouldCallClient() throws Exception {
        mockMvc.perform(get("/items/10")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());

        verify(itemClient).get(1L, 10L);
    }

    @Test
    void get_withInvalidItemId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/0")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void getAllByOwner_withValidOwnerId_shouldCallClient() throws Exception {
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "5"))
                .andExpect(status().isOk());

        verify(itemClient).getAllByOwner(5L);
    }

    @Test
    void search_withText_shouldCallClient() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk());

        verify(itemClient).search("дрель");
    }

    @Test
    void update_withValidData_shouldCallClient() throws Exception {
        mockMvc.perform(patch("/items/20")
                        .header("X-Sharer-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemUpdate)))
                .andExpect(status().isOk());

        verify(itemClient).update(eq(2L), eq(20L), any(ItemUpdateDTO.class));
    }

    @Test
    void delete_withValidIds_shouldCallClient() throws Exception {
        mockMvc.perform(delete("/items/30")
                        .header("X-Sharer-User-Id", "3"))
                .andExpect(status().isNoContent());

        verify(itemClient).delete(3L, 30L);
    }

    @Test
    void createComment_withValidData_shouldCallClient() throws Exception {
        mockMvc.perform(post("/items/40/comment")
                        .header("X-Sharer-User-Id", "4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validComment)))
                .andExpect(status().isCreated());

        verify(itemClient).createComment(eq(4L), eq(40L), any(CommentCreateDTO.class));
    }

    @Test
    void createComment_withBlankText_shouldReturnBadRequest() throws Exception {
        CommentCreateDTO invalid = new CommentCreateDTO("");
        mockMvc.perform(post("/items/40/comment")
                        .header("X-Sharer-User-Id", "4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

// ========== Валидация полей ItemCreateDTO ==========

    @Test
    void createItem_Validation_NegativeRequestId() throws Exception {
        ItemCreateDTO invalid = new ItemCreateDTO("Tool", "Useful", true, -1L);
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид запроса должен быть больше 0")));
        verifyNoInteractions(itemClient);
    }

    @Test
    void createItem_Validation_LongDescription() throws Exception {
        String longDesc = "a".repeat(1001);
        ItemCreateDTO invalid = new ItemCreateDTO("Tool", longDesc, true, null);
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Описание не может быть длиннее")));
        verifyNoInteractions(itemClient);
    }

// ========== Валидация параметров пути и заголовков ==========

    @Test
    void getItem_Validation_InvalidUserId() throws Exception {
        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", -1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид пользователя должен быть больше 0")));
        verifyNoInteractions(itemClient);
    }

    @Test
    void updateItem_Validation_InvalidOwnerId() throws Exception {
        ItemUpdateDTO update = new ItemUpdateDTO("New Name", null, null, null);
        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид владельца должен быть больше 0")));
        verifyNoInteractions(itemClient);
    }

    @Test
    void updateItem_Validation_NegativeRequestId() throws Exception {
        ItemUpdateDTO invalid = new ItemUpdateDTO("New Name", null, false, -5L);
        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид запроса должен быть больше 0")));
        verifyNoInteractions(itemClient);
    }

    @Test
    void deleteItem_Validation_InvalidOwnerId() throws Exception {
        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", -1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид владельца должен быть больше 0")));
        verifyNoInteractions(itemClient);
    }

// ========== Валидация комментариев ==========

    @Test
    void createComment_Validation_TooLongText() throws Exception {
        String longText = "a".repeat(1001);
        CommentCreateDTO invalid = new CommentCreateDTO(longText);
        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Текст отзыва не может быть длиннее")));
        verifyNoInteractions(itemClient);
    }

    @Test
    void createComment_Validation_InvalidUserId() throws Exception {
        CommentCreateDTO comment = new CommentCreateDTO("Great!");
        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид пользователя должен быть больше 0")));
        verifyNoInteractions(itemClient);
    }

    @Test
    void createComment_Validation_InvalidItemId() throws Exception {
        CommentCreateDTO comment = new CommentCreateDTO("Great!");
        mockMvc.perform(post("/items/0/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид вещи должен быть больше 0")));
        verifyNoInteractions(itemClient);
    }

// ========== Проксирование ошибок от клиента ==========

    @Test
    void getItem_NotFound_ShouldProxyClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\":\"Item not found\"}");
        when(itemClient.get(1L, 999L)).thenReturn(clientResponse);

        mockMvc.perform(get("/items/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Item not found\"}"));
    }

    @Test
    void updateItem_PermissionDenied_ShouldProxyClientResponse() throws Exception {
        ItemUpdateDTO update = new ItemUpdateDTO("New Name", null, null, null);
        ResponseEntity<Object> clientResponse = ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("{\"error\":\"Not owner\"}");
        when(itemClient.update(2L, 1L, update)).thenReturn(clientResponse);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden())
                .andExpect(content().json("{\"error\":\"Not owner\"}"));
    }
}