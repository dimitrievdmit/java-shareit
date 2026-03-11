package ru.yandex.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.shareit.exception.NotFoundException;
import ru.yandex.practicum.shareit.exception.PermissionException;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Test
    void getItem_Success() throws Exception {
        when(itemService.get(anyLong())).thenReturn(itemResponseDTO);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Tool"));

        verify(itemService, times(1)).get(1L);
    }

    @Test
    void getAllByOwner_Success() throws Exception {
        List<ItemResponseDTO> items = List.of(itemResponseDTO);
        when(itemService.getAllByOwner(anyLong())).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(itemService, times(1)).getAllByOwner(1L);
    }

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
    void deleteItem_Success() throws Exception {
        doNothing().when(itemService).delete(anyLong(), anyLong());

        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNoContent());

        verify(itemService, times(1)).delete(1L, 1L);
    }

    @Test
    void getItem_NotFound() throws Exception {
        when(itemService.get(anyLong())).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/items/999"))
                .andExpect(status().isNotFound());
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
    void createItem_Validation_EmptyName() throws Exception {
        ItemCreateDTO invalidDTO = new ItemCreateDTO(
                "", "Useful tool", true, 1L // Пустое имя
        );

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Название не может быть пустым")));
    }

    @Test
    void createItem_Validation_NullAvailable() throws Exception {
        ItemCreateDTO invalidDTO = new ItemCreateDTO(
                "Tool", "Useful tool", null, 1L // Null available
        );

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Флаг доступности для бронирования не может быть пустым")));
    }

    @Test
    void createItem_Validation_NegativeRequestId() throws Exception {
        @SuppressWarnings("DataFlowIssue") ItemCreateDTO invalidDTO = new ItemCreateDTO(
                "Tool", "Useful tool", true, -1L // Отрицательный requestId
        );

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид запроса должен быть больше 0")));
    }

    @Test
    void updateItem_Validation_PartialUpdate() throws Exception {
        // Частичное обновление — только name
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

    @Test
    void searchItems_Validation_NullText() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItem_Validation_InvalidId() throws Exception {
        mockMvc.perform(get("/items/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид вещи должен быть больше 0")));

        mockMvc.perform(get("/items/-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид вещи должен быть больше 0")));
    }

    @Test
    void deleteItem_Validation_InvalidOwnerId() throws Exception {
        doNothing().when(itemService).delete(anyLong(), anyLong());

        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", -1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид владельца должен быть больше 0")));
    }

    @Test
    void createItem_Validation_MissingHeader() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_Validation_LongDescription() throws Exception {
        String longDescription = "a".repeat(1001); // Превышает MAX_DESCRIPTION_LENGTH
        ItemCreateDTO invalidDTO = new ItemCreateDTO(
                "Tool", longDescription, true, 1L
        );

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Описание не может быть длиннее")));
    }

    @Test
    void updateItem_Validation_NegativeRequestId() throws Exception {
        @SuppressWarnings("DataFlowIssue") ItemUpdateDTO invalidDTO = new ItemUpdateDTO(
                "Updated Tool", "Description", false, -5L
        );

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид запроса должен быть больше 0")));
    }
}
