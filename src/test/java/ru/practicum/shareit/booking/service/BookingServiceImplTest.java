package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingCreateDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.PermissionException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validator.Validator;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemService itemService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private final LocalDateTime now = LocalDateTime.now();
    private final Long bookingId = 100L;
    private final Long userId = 10L;
    private final Long ownerId = 20L;
    private final Long itemId = 1L;

    private Item createItem(Long id, Long ownerId, Boolean available) {
        return new Item(id, ownerId, "Item", "Desc", available, null);
    }

    private User createUser(Long id) {
        return new User(id, "Name", "email@mail.com");
    }

    private Booking createBooking(Long id, LocalDateTime start, LocalDateTime end, BookingStatus status,
                                  Item item, User booker) {
        Booking b = new Booking();
        b.setId(id);
        b.setStart(start);
        b.setEnd(end);
        b.setStatus(status);
        b.setItem(item);
        b.setBooker(booker);
        return b;
    }

    // --- create ---
    @Test
    void create_ValidData_ShouldCreateBooking() {
        BookingCreateDTO dto = new BookingCreateDTO(itemId, now.plusDays(1), now.plusDays(2));
        Item item = createItem(itemId, ownerId, true);
        User booker = createUser(userId);
        Booking savedBooking = createBooking(bookingId, dto.start(), dto.end(), BookingStatus.WAITING, item, booker);

        doNothing().when(userService).throwIfNotExists(userId);
        doNothing().when(itemService).isExistsOrElseThrow(itemId);
        when(itemRepository.getReferenceById(itemId)).thenReturn(item);
        when(userRepository.getReferenceById(userId)).thenReturn(booker);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingResponseDTO result = bookingService.create(dto, userId);

        assertThat(result.id()).isEqualTo(bookingId);
        assertThat(result.status()).isEqualTo(BookingStatus.WAITING);
        verify(bookingRepository).save(any(Booking.class));
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        Booking captured = captor.getValue();
        assertThat(captured.getStart()).isEqualTo(dto.start());
        assertThat(captured.getEnd()).isEqualTo(dto.end());
        assertThat(captured.getItem()).isEqualTo(item);
        assertThat(captured.getBooker()).isEqualTo(booker);
    }

    @Test
    void create_ItemNotAvailable_ShouldThrowValidationException() {
        BookingCreateDTO dto = new BookingCreateDTO(itemId, now.plusDays(1), now.plusDays(2));
        Item item = createItem(itemId, ownerId, false);

        doNothing().when(userService).throwIfNotExists(userId);
        doNothing().when(itemService).isExistsOrElseThrow(itemId);
        when(itemRepository.getReferenceById(itemId)).thenReturn(item);

        assertThatThrownBy(() -> bookingService.create(dto, userId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Бронируемая вещь должна быть доступна для бронирования");
    }

    @Test
    void create_StartEqualsEnd_ShouldThrowValidationException() {
        LocalDateTime start = now.plusDays(1);
        BookingCreateDTO dto = new BookingCreateDTO(itemId, start, start);

        doNothing().when(userService).throwIfNotExists(userId);
        doNothing().when(itemService).isExistsOrElseThrow(itemId);

        assertThatThrownBy(() -> bookingService.create(dto, userId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("не должна быть равна");
    }

    @Test
    void create_StartAfterEnd_ShouldThrowValidationException() {
        BookingCreateDTO dto = new BookingCreateDTO(itemId, now.plusDays(2), now.plusDays(1));

        doNothing().when(userService).throwIfNotExists(userId);
        doNothing().when(itemService).isExistsOrElseThrow(itemId);

        assertThatThrownBy(() -> bookingService.create(dto, userId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("не может быть позже");
    }

    // --- updateStatus ---
    @Test
    void updateStatus_OwnerApproves_ShouldUpdateStatus() {
        Item item = createItem(itemId, ownerId, true);
        User booker = createUser(userId);
        Booking booking = createBooking(bookingId, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, item, booker);
        Booking updatedBooking = createBooking(bookingId, booking.getStart(), booking.getEnd(), BookingStatus.APPROVED, item, booker);

        when(bookingRepository.existsById(bookingId)).thenReturn(true);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        doNothing().when(userService).throwIfNotExists(ownerId);
        when(bookingRepository.save(any(Booking.class))).thenReturn(updatedBooking);

        BookingResponseDTO result = bookingService.updateStatus(bookingId, ownerId, BookingStatus.APPROVED);

        assertThat(result.status()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository).save(booking);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void updateStatus_NotOwner_ShouldThrowPermissionException() {
        Item item = createItem(itemId, ownerId, true);
        User booker = createUser(userId);
        Booking booking = createBooking(bookingId, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, item, booker);
        Long wrongUserId = 999L;

        when(bookingRepository.existsById(bookingId)).thenReturn(true);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        String expectedMsg = String.format(Validator.BOOKING_PERMISSION_ERR_TEXT, wrongUserId, bookingId);
        assertThatThrownBy(() -> bookingService.updateStatus(bookingId, wrongUserId, BookingStatus.APPROVED))
                .isInstanceOf(PermissionException.class)
                .hasMessageContaining(expectedMsg);
    }

    // --- getById ---
    @Test
    void getById_AsBooker_ShouldReturnBooking() {
        Item item = createItem(itemId, ownerId, true);
        User booker = createUser(userId);
        Booking booking = createBooking(bookingId, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, item, booker);

        when(bookingRepository.existsById(bookingId)).thenReturn(true);
        doNothing().when(userService).throwIfNotExists(userId);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDTO result = bookingService.getById(bookingId, userId);

        assertThat(result.id()).isEqualTo(bookingId);
    }

    @Test
    void getById_AsOwner_ShouldReturnBooking() {
        Item item = createItem(itemId, ownerId, true);
        User booker = createUser(999L);
        Booking booking = createBooking(bookingId, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, item, booker);

        when(bookingRepository.existsById(bookingId)).thenReturn(true);
        doNothing().when(userService).throwIfNotExists(ownerId);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDTO result = bookingService.getById(bookingId, ownerId);

        assertThat(result.id()).isEqualTo(bookingId);
    }

    @Test
    void getById_NotBookerOrOwner_ShouldThrowPermissionException() {
        Item item = createItem(itemId, ownerId, true);
        User booker = createUser(999L);
        Booking booking = createBooking(bookingId, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, item, booker);
        Long wrongUserId = 888L;

        when(bookingRepository.existsById(bookingId)).thenReturn(true);
        doNothing().when(userService).throwIfNotExists(wrongUserId);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        String expectedMsg = String.format(Validator.BOOKING_PERMISSION_ERR_TEXT, wrongUserId, bookingId);
        assertThatThrownBy(() -> bookingService.getById(bookingId, wrongUserId))
                .isInstanceOf(PermissionException.class)
                .hasMessageContaining(expectedMsg);
    }

    // --- getByBooker ---
    @Test
    void getByBooker_StateAll_ShouldReturnAllSorted() {
        Item item = createItem(1L, ownerId, true);
        User booker = createUser(userId);
        List<Booking> bookings = List.of(
                createBooking(1L, now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED, item, booker),
                createBooking(2L, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, item, booker)
        );
        doNothing().when(userService).throwIfNotExists(userId);
        when(bookingRepository.findByBookerId(eq(userId), any(Sort.class))).thenReturn(bookings);

        Collection<BookingResponseDTO> result = bookingService.getByBooker(userId, BookingState.ALL);

        assertThat(result).hasSize(2);
        verify(bookingRepository).findByBookerId(eq(userId), any(Sort.class));
    }

    @Test
    void getByBooker_StatePast_ShouldCallCorrectRepositoryMethod() {
        doNothing().when(userService).throwIfNotExists(userId);
        bookingService.getByBooker(userId, BookingState.PAST);
        verify(bookingRepository).findByBookerIdAndEndIsBefore(eq(userId), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getByBooker_StateCurrent_ShouldCallCorrectRepositoryMethod() {
        doNothing().when(userService).throwIfNotExists(userId);
        bookingService.getByBooker(userId, BookingState.CURRENT);
        verify(bookingRepository).findByBookerIdAndActiveOnDate(eq(userId), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getByBooker_StateFuture_ShouldCallCorrectRepositoryMethod() {
        doNothing().when(userService).throwIfNotExists(userId);
        bookingService.getByBooker(userId, BookingState.FUTURE);
        verify(bookingRepository).findByBookerIdAndStartIsAfter(eq(userId), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getByBooker_StateWaiting_ShouldCallCorrectRepositoryMethod() {
        doNothing().when(userService).throwIfNotExists(userId);
        bookingService.getByBooker(userId, BookingState.WAITING);
        verify(bookingRepository).findByBookerIdAndStatusIs(eq(userId), eq(BookingStatus.WAITING), any(Sort.class));
    }

    @Test
    void getByBooker_StateRejected_ShouldCallCorrectRepositoryMethod() {
        doNothing().when(userService).throwIfNotExists(userId);
        bookingService.getByBooker(userId, BookingState.REJECTED);
        verify(bookingRepository).findByBookerIdAndStatusIs(eq(userId), eq(BookingStatus.REJECTED), any(Sort.class));
    }

    // --- getByItemOwner ---
    @Test
    void getByItemOwner_StateAll_ShouldReturnAllSorted() {
        doNothing().when(userService).throwIfNotExists(ownerId);
        bookingService.getByItemOwner(ownerId, BookingState.ALL);
        verify(bookingRepository).findByItemOwnerId(eq(ownerId), any(Sort.class));
    }

    @Test
    void getByItemOwner_StatePast_ShouldCallCorrectRepositoryMethod() {
        doNothing().when(userService).throwIfNotExists(ownerId);
        bookingService.getByItemOwner(ownerId, BookingState.PAST);
        verify(bookingRepository).findByItemOwnerIdAndEndIsBefore(eq(ownerId), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getByItemOwner_StateCurrent_ShouldCallCorrectRepositoryMethod() {
        doNothing().when(userService).throwIfNotExists(ownerId);
        bookingService.getByItemOwner(ownerId, BookingState.CURRENT);
        verify(bookingRepository).findByItemOwnerIdAndActiveOnDate(eq(ownerId), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getByItemOwner_StateFuture_ShouldCallCorrectRepositoryMethod() {
        doNothing().when(userService).throwIfNotExists(ownerId);
        bookingService.getByItemOwner(ownerId, BookingState.FUTURE);
        verify(bookingRepository).findByItemOwnerIdAndStartIsAfter(eq(ownerId), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getByItemOwner_StateWaiting_ShouldCallCorrectRepositoryMethod() {
        doNothing().when(userService).throwIfNotExists(ownerId);
        bookingService.getByItemOwner(ownerId, BookingState.WAITING);
        verify(bookingRepository).findByItemOwnerIdAndStatusIs(eq(ownerId), eq(BookingStatus.WAITING), any(Sort.class));
    }

    // --- throwIfNotExists ---
    @Test
    void throwIfNotExists_WhenExists_ShouldNotThrow() {
        when(bookingRepository.existsById(bookingId)).thenReturn(true);
        bookingService.isExistsOrElseThrow(bookingId);
    }

    @Test
    void throwIfNotExists_WhenNotExists_ShouldThrowNotFoundException() {
        when(bookingRepository.existsById(bookingId)).thenReturn(false);
        assertThatThrownBy(() -> bookingService.isExistsOrElseThrow(bookingId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Бронирование с id = " + bookingId + " не найден");
    }
}