package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.PermissionException;
import ru.practicum.shareit.item.dto.item.ItemResponseDTO;
import ru.practicum.shareit.user.dto.UserResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime futureStart = now.plusDays(1);
    private final LocalDateTime futureEnd = now.plusDays(2);

    private final ItemResponseDTO itemResponse = new ItemResponseDTO(1L, "Item", "Desc", true, null);
    private final UserResponseDTO userResponse = new UserResponseDTO(10L, "User", "user@mail.com");

    private final BookingResponseDTO bookingResponse = new BookingResponseDTO(
            100L, futureStart, futureEnd, itemResponse, userResponse, BookingStatus.WAITING
    );

    private final BookingCreateDTO bookingCreate = new BookingCreateDTO(1L, futureStart, futureEnd);

    @Test
    void createBooking_Success() throws Exception {
        when(bookingService.create(any(BookingCreateDTO.class), eq(1L)))
                .thenReturn(bookingResponse);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).create(any(BookingCreateDTO.class), eq(1L));
    }

    @Test
    void getBookingById_Success() throws Exception {
        when(bookingService.getById(eq(100L), eq(1L))).thenReturn(bookingResponse);

        mockMvc.perform(get("/bookings/100")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).getById(100L, 1L);
    }

    @Test
    void getBookingById_NotFound() throws Exception {
        when(bookingService.getById(eq(999L), eq(1L))).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingById_Forbidden() throws Exception {
        when(bookingService.getById(eq(100L), eq(2L))).thenThrow(PermissionException.class);

        mockMvc.perform(get("/bookings/100")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBookingsByBooker_DefaultState() throws Exception {
        List<BookingResponseDTO> bookings = List.of(bookingResponse);
        when(bookingService.getByBooker(eq(1L), eq(BookingState.ALL))).thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void getBookingsByBooker_WithState() throws Exception {
        List<BookingResponseDTO> bookings = List.of(bookingResponse);
        when(bookingService.getByBooker(eq(1L), eq(BookingState.PAST))).thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "PAST"))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingsByOwner_Success() throws Exception {
        List<BookingResponseDTO> bookings = List.of(bookingResponse);
        when(bookingService.getByItemOwner(eq(10L), eq(BookingState.ALL))).thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void updateBookingStatus_Approve() throws Exception {
        BookingResponseDTO approved = new BookingResponseDTO(
                100L, futureStart, futureEnd, itemResponse, userResponse, BookingStatus.APPROVED
        );
        when(bookingService.updateStatus(eq(100L), eq(1L), eq(BookingStatus.APPROVED)))
                .thenReturn(approved);

        mockMvc.perform(patch("/bookings/100")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void updateBookingStatus_Reject() throws Exception {
        BookingResponseDTO rejected = new BookingResponseDTO(
                100L, futureStart, futureEnd, itemResponse, userResponse, BookingStatus.REJECTED
        );
        when(bookingService.updateStatus(eq(100L), eq(1L), eq(BookingStatus.REJECTED)))
                .thenReturn(rejected);

        mockMvc.perform(patch("/bookings/100")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void updateBookingStatus_NotOwner() throws Exception {
        when(bookingService.updateStatus(eq(100L), eq(2L), eq(BookingStatus.APPROVED)))
                .thenThrow(PermissionException.class);

        mockMvc.perform(patch("/bookings/100")
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }
}