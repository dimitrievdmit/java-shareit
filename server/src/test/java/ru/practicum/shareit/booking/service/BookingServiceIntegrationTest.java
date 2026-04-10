package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.PermissionException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User booker;
    private Item item;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(new User(null, "Owner", "owner@test.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@test.com"));
        item = itemRepository.save(new Item(null, owner.getId(), "Drill", "Power drill", true, null));
    }

    // --- create ---
    @Test
    void create_ValidData_ShouldSaveBooking() {
        BookingCreateDTO dto = new BookingCreateDTO(item.getId(), now.plusDays(1), now.plusDays(2));
        BookingResponseDTO created = bookingService.create(dto, booker.getId());

        assertThat(created.id()).isNotNull();
        assertThat(created.status()).isEqualTo(BookingStatus.WAITING);
        assertThat(created.item().id()).isEqualTo(item.getId());
        assertThat(created.booker().id()).isEqualTo(booker.getId());

        Booking fromDb = bookingRepository.findById(created.id()).orElseThrow();
        assertThat(fromDb.getStart()).isEqualTo(now.plusDays(1));
        assertThat(fromDb.getEnd()).isEqualTo(now.plusDays(2));
    }

    @Test
    void create_ItemNotAvailable_ShouldThrowValidationException() {
        item.setAvailable(false);
        itemRepository.save(item);
        BookingCreateDTO dto = new BookingCreateDTO(item.getId(), now.plusDays(1), now.plusDays(2));
        assertThatThrownBy(() -> bookingService.create(dto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Бронируемая вещь должна быть доступна");
    }

    // --- updateStatus ---
    @Test
    void updateStatus_OwnerApproves_ShouldChangeStatus() {
        BookingCreateDTO dto = new BookingCreateDTO(item.getId(), now.plusDays(1), now.plusDays(2));
        BookingResponseDTO created = bookingService.create(dto, booker.getId());

        BookingResponseDTO updated = bookingService.updateStatus(created.id(), owner.getId(), BookingStatus.APPROVED);

        assertThat(updated.status()).isEqualTo(BookingStatus.APPROVED);
        Booking fromDb = bookingRepository.findById(created.id()).orElseThrow();
        assertThat(fromDb.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void updateStatus_NotOwner_ShouldThrowPermissionException() {
        BookingCreateDTO dto = new BookingCreateDTO(item.getId(), now.plusDays(1), now.plusDays(2));
        BookingResponseDTO created = bookingService.create(dto, booker.getId());
        Long wrongUserId = 999L;
        assertThatThrownBy(() -> bookingService.updateStatus(created.id(), wrongUserId, BookingStatus.APPROVED))
                .isInstanceOf(PermissionException.class);
    }

    // --- getById ---
    @Test
    void getById_AsBooker_ShouldReturnBooking() {
        BookingCreateDTO dto = new BookingCreateDTO(item.getId(), now.plusDays(1), now.plusDays(2));
        BookingResponseDTO created = bookingService.create(dto, booker.getId());

        BookingResponseDTO found = bookingService.getById(created.id(), booker.getId());

        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.booker().id()).isEqualTo(booker.getId());
    }

    @Test
    void getById_AsOwner_ShouldReturnBooking() {
        BookingCreateDTO dto = new BookingCreateDTO(item.getId(), now.plusDays(1), now.plusDays(2));
        BookingResponseDTO created = bookingService.create(dto, booker.getId());

        BookingResponseDTO found = bookingService.getById(created.id(), owner.getId());

        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.item().id()).isEqualTo(item.getId());
    }

    @Test
    void getById_NotBookerOrOwner_ShouldThrowPermissionException() {
        User stranger = userRepository.save(new User(null, "Stranger", "stranger@test.com"));
        BookingCreateDTO dto = new BookingCreateDTO(item.getId(), now.plusDays(1), now.plusDays(2));
        BookingResponseDTO created = bookingService.create(dto, booker.getId());

        assertThatThrownBy(() -> bookingService.getById(created.id(), stranger.getId()))
                .isInstanceOf(PermissionException.class);
    }

    // --- getByBooker ---
    @Test
    void getByBooker_StateAll_ShouldReturnAllBookerBookings() {
        BookingCreateDTO dto1 = new BookingCreateDTO(item.getId(), now.plusDays(1), now.plusDays(2));
        BookingCreateDTO dto2 = new BookingCreateDTO(item.getId(), now.plusDays(3), now.plusDays(4));
        bookingService.create(dto1, booker.getId());
        bookingService.create(dto2, booker.getId());

        Collection<BookingResponseDTO> result = bookingService.getByBooker(booker.getId(), BookingState.ALL);

        assertThat(result).hasSize(2);
    }

    @Test
    void getByBooker_StatePast_ShouldReturnOnlyPast() {
        // Создаём прошлое бронирование
        BookingCreateDTO pastDto = new BookingCreateDTO(item.getId(), now.minusDays(3), now.minusDays(2));
        bookingService.create(pastDto, booker.getId());
        // Будущее бронирование
        BookingCreateDTO futureDto = new BookingCreateDTO(item.getId(), now.plusDays(1), now.plusDays(2));
        bookingService.create(futureDto, booker.getId());

        // Сначала обновляем статус прошлого бронирования на APPROVED (чтобы оно было завершённым)
        Booking pastBooking = bookingRepository.findAll().stream()
                .filter(b -> b.getStart().isBefore(now))
                .findFirst().orElseThrow();
        bookingService.updateStatus(pastBooking.getId(), owner.getId(), BookingStatus.APPROVED);

        Collection<BookingResponseDTO> result = bookingService.getByBooker(booker.getId(), BookingState.PAST);
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().start()).isBefore(now);
    }

    // --- getByItemOwner ---
    @Test
    void getByItemOwner_StateAll_ShouldReturnAllOwnerBookings() {
        BookingCreateDTO dto = new BookingCreateDTO(item.getId(), now.plusDays(1), now.plusDays(2));
        bookingService.create(dto, booker.getId());

        Collection<BookingResponseDTO> result = bookingService.getByItemOwner(owner.getId(), BookingState.ALL);

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().item().id()).isEqualTo(item.getId());
    }

    @Test
    void getByItemOwner_StateWaiting_ShouldReturnOnlyWaiting() {
        BookingCreateDTO dto = new BookingCreateDTO(item.getId(), now.plusDays(1), now.plusDays(2));
        bookingService.create(dto, booker.getId());

        Collection<BookingResponseDTO> result = bookingService.getByItemOwner(owner.getId(), BookingState.WAITING);

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().status()).isEqualTo(BookingStatus.WAITING);
    }
}