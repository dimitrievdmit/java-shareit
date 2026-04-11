package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.PermissionException;
import ru.practicum.shareit.item.dto.comment.CommentCreateDTO;
import ru.practicum.shareit.item.dto.comment.CommentResponseDTO;
import ru.practicum.shareit.item.dto.item.ItemCreateDTO;
import ru.practicum.shareit.item.dto.item.ItemResponseDTO;
import ru.practicum.shareit.item.dto.item.ItemUpdateDTO;
import ru.practicum.shareit.item.dto.item.ItemWithBookingDTO;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private final ItemService itemService;

    private final ObjectMapper objectMapper;

    private final ItemResponseDTO itemResponseDTO = new ItemResponseDTO(
            1L, "Tool", "Useful tool", true, 1L
    );

    private final ItemCreateDTO itemCreateDTO = new ItemCreateDTO(
            "Tool", "Useful tool", true, 1L
    );

    private final ItemUpdateDTO itemUpdateDTO = new ItemUpdateDTO(
            "Updated Tool", "Updated description", false, 2L
    );

    private final ItemWithBookingDTO itemWithBookingDTO = new ItemWithBookingDTO(
            1L, 1L, "Tool", "Useful tool", true, 1L,
            null, null, List.of()
    );

    private final CommentResponseDTO commentResponseDTO = new CommentResponseDTO(
            1L, "Great item!", "User", LocalDateTime.now()
    );

    private final CommentCreateDTO commentCreateDTO = new CommentCreateDTO("Great item!");

    // --- Тесты для create ---
    @Test
    void createItem_Success() throws Exception {
        when(itemService.create(any(ItemCreateDTO.class), anyLong()))
                .thenReturn(itemResponseDTO);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Tool"));

        verify(itemService, times(1)).create(any(ItemCreateDTO.class), eq(1L));
    }

    // --- Тесты для get ---
    @Test
    void getItem_Success() throws Exception {
        when(itemService.get(anyLong(), anyLong())).thenReturn(itemWithBookingDTO);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Tool"))
                .andExpect(jsonPath("$.ownerId").value(1L));

        verify(itemService, times(1)).get(1L, 1L);
    }

    @Test
    void getItem_NotFound() throws Exception {
        when(itemService.get(anyLong(), anyLong())).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/items/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    // --- Тесты для getAllByOwner ---
    @Test
    void getAllByOwner_Success() throws Exception {
        List<ItemWithBookingDTO> items = List.of(itemWithBookingDTO);
        when(itemService.getAllByOwner(anyLong())).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].ownerId").value(1L));

        verify(itemService, times(1)).getAllByOwner(1L);
    }

    // --- Тесты для search ---
    @Test
    void searchItems_Success() throws Exception {
        List<ItemResponseDTO> items = List.of(itemResponseDTO);
        when(itemService.search(anyString())).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "tool"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tool"));

        verify(itemService, times(1)).search("tool");
    }

    // --- Тесты для update ---
    @Test
    void updateItem_Success() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any(ItemUpdateDTO.class)))
                .thenReturn(itemResponseDTO);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tool"));

        verify(itemService, times(1))
                .update(eq(1L), eq(1L), any(ItemUpdateDTO.class));
    }

    @Test
    void updateItem_PermissionDenied() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any(ItemUpdateDTO.class)))
                .thenThrow(PermissionException.class);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateItem_Validation_PartialUpdate() throws Exception {
        ItemUpdateDTO partialUpdate = new ItemUpdateDTO("New Name", null, null, null);

        when(itemService.update(anyLong(), anyLong(), any(ItemUpdateDTO.class)))
                .thenReturn(itemResponseDTO);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk());

        verify(itemService, times(1)).update(eq(1L), eq(1L), any(ItemUpdateDTO.class));
    }

    // --- Тесты для delete ---
    @Test
    void deleteItem_Success() throws Exception {
        doNothing().when(itemService).delete(anyLong(), anyLong());

        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNoContent());

        verify(itemService, times(1)).delete(1L, 1L);
    }

    // --- Тесты для createComment ---
    @Test
    void createComment_Success() throws Exception {
        when(itemService.createComment(anyLong(), anyLong(), anyString()))
                .thenReturn(commentResponseDTO);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("User"));

        verify(itemService, times(1)).createComment(1L, 1L, "Great item!");
    }

    @Test
    void createComment_ItemNotFound() throws Exception {
        when(itemService.createComment(anyLong(), anyLong(), anyString()))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(post("/items/999/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_PermissionDenied() throws Exception {
        when(itemService.createComment(anyLong(), anyLong(), anyString()))
                .thenThrow(PermissionException.class);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDTO)))
                .andExpect(status().isForbidden());
    }
}