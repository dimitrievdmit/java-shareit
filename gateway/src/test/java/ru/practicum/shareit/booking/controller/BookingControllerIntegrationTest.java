package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDTO;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.common.controller.GatewayIntegrationTestBase;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerIntegrationTest extends GatewayIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_validRequest_shouldReturnClientResponse() throws Exception {
        BookingCreateDTO requestDto = new BookingCreateDTO(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        ResponseEntity<Object> clientResponse = ResponseEntity.status(HttpStatus.CREATED)
                .body("{\"id\":1,\"status\":\"WAITING\"}");

        when(bookingClient.create(eq(1L), any(BookingCreateDTO.class)))
                .thenReturn(clientResponse);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"id\":1,\"status\":\"WAITING\"}"));
    }

    @Test
    void getById_validRequest_shouldReturnClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.ok("{\"id\":10,\"status\":\"APPROVED\"}");
        when(bookingClient.getById(2L, 10L)).thenReturn(clientResponse);

        mockMvc.perform(get("/bookings/10")
                        .header("X-Sharer-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":10,\"status\":\"APPROVED\"}"));
    }

    @Test
    void getByBooker_validRequest_shouldReturnClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.ok("[{\"id\":1},{\"id\":2}]");
        when(bookingClient.getByBooker(3L, BookingState.ALL)).thenReturn(clientResponse);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "3")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":1},{\"id\":2}]"));
    }
}