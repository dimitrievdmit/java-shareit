package ru.practicum.shareit.booking.controller;

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
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingCreateDTO;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    private BookingCreateDTO validBookingCreate;

    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime futureStart = now.plusDays(1);
    private final LocalDateTime futureEnd = now.plusDays(10);

    @BeforeEach
    void setUp() {
        validBookingCreate = new BookingCreateDTO(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
    }

    @Test
    void create_withValidData_shouldCallClient() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookingCreate)))
                .andExpect(status().isCreated());

        verify(bookingClient).create(eq(1L), any(BookingCreateDTO.class));
    }

    @Test
    void create_withStartAfterEnd_shouldReturnBadRequest() throws Exception {
        BookingCreateDTO invalid = new BookingCreateDTO(
                1L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1)
        );
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void create_withNullItemId_shouldReturnBadRequest() throws Exception {
        BookingCreateDTO invalid = new BookingCreateDTO(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void getById_withValidIds_shouldCallClient() throws Exception {
        mockMvc.perform(get("/bookings/10")
                        .header("X-Sharer-User-Id", "5"))
                .andExpect(status().isOk());

        verify(bookingClient).getById(5L, 10L);
    }

    @Test
    void getByBooker_withDefaultState_shouldCallClient() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "2"))
                .andExpect(status().isOk());

        verify(bookingClient).getByBooker(2L, BookingState.ALL);
    }

    @Test
    void getByBooker_withInvalidState_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "2")
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void getByItemOwner_withValidData_shouldCallClient() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", "3")
                        .param("state", "PAST"))
                .andExpect(status().isOk());

        verify(bookingClient).getByItemOwner(3L, BookingState.PAST);
    }

    @Test
    void updateStatus_withValidData_shouldCallClient() throws Exception {
        mockMvc.perform(patch("/bookings/20")
                        .header("X-Sharer-User-Id", "4")
                        .param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingClient).updateStatus(4L, 20L, true);
    }

// ========== Валидация полей BookingCreateDTO ==========

    @Test
    void createBooking_Validation_InvalidItemId() throws Exception {
        BookingCreateDTO invalid = new BookingCreateDTO(-1L, futureStart, futureEnd);
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид бронируемой вещи должен быть больше 0")));
        verifyNoInteractions(bookingClient);
    }

    @Test
    void createBooking_Validation_NullStart() throws Exception {
        BookingCreateDTO invalid = new BookingCreateDTO(1L, null, futureEnd);
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Дата и время начала бронирования должна быть указана")));
        verifyNoInteractions(bookingClient);
    }

    @Test
    void createBooking_Validation_PastStart() throws Exception {
        BookingCreateDTO invalid = new BookingCreateDTO(1L, now.minusDays(1), futureEnd);
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("не может быть в прошлом")));
        verifyNoInteractions(bookingClient);
    }

    @Test
    void createBooking_Validation_NullEnd() throws Exception {
        BookingCreateDTO invalid = new BookingCreateDTO(1L, futureStart, null);
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Дата и время конца бронирования должна быть указана")));
        verifyNoInteractions(bookingClient);
    }

    @Test
    void createBooking_Validation_EndBeforeStart() throws Exception {
        // Требует аннотации @ValidBookingDates на классе BookingCreateDTO
        BookingCreateDTO invalid = new BookingCreateDTO(1L, futureEnd, futureStart);
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("должна быть позже")));
        verifyNoInteractions(bookingClient);
    }

// ========== Валидация параметров пути и заголовков ==========

    @Test
    void getBookingById_InvalidBookingId() throws Exception {
        mockMvc.perform(get("/bookings/0")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид бронирования должен быть больше 0")));
        verifyNoInteractions(bookingClient);
    }

    @Test
    void getBookingById_InvalidUserId() throws Exception {
        mockMvc.perform(get("/bookings/100")
                        .header("X-Sharer-User-Id", -1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид пользователя должен быть больше 0")));
        verifyNoInteractions(bookingClient);
    }

// ========== Проксирование ошибок от клиента (404, 403) ==========

    @Test
    void getBookingById_NotFound_ShouldProxyClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\":\"Booking not found\"}");
        when(bookingClient.getById(1L, 999L)).thenReturn(clientResponse);

        mockMvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Booking not found\"}"));
    }

    @Test
    void getBookingById_Forbidden_ShouldProxyClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("{\"error\":\"Access denied\"}");
        when(bookingClient.getById(2L, 100L)).thenReturn(clientResponse);

        mockMvc.perform(get("/bookings/100")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isForbidden())
                .andExpect(content().json("{\"error\":\"Access denied\"}"));
    }

    @Test
    void updateBookingStatus_InvalidBookingId() throws Exception {
        mockMvc.perform(patch("/bookings/0")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Ид бронирования должен быть больше 0")));
        verifyNoInteractions(bookingClient);
    }

    @Test
    void updateBookingStatus_NotFound_ShouldProxyClientResponse() throws Exception {
        ResponseEntity<Object> clientResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        when(bookingClient.updateStatus(1L, 999L, true)).thenReturn(clientResponse);

        mockMvc.perform(patch("/bookings/999")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }
}