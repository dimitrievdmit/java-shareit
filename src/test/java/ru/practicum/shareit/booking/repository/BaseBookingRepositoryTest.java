package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public abstract class BaseBookingRepositoryTest {

    protected BookingRepository bookingRepository;

    // Эти методы должны быть реализованы в конкретном тестовом классе для получения сохранённых сущностей
    protected abstract Item getTestItem();

    protected abstract User getTestBooker();

    protected abstract User getOtherBooker();

    protected abstract Long getOwnerId();

    // Вспомогательный метод для создания и сохранения Booking через репозиторий
    protected Booking createAndSaveBooking(LocalDateTime start, LocalDateTime end, BookingStatus status, Item item, User booker) {
        Booking booking = new Booking(null, start, end, item, booker, status);
        return bookingRepository.saveAndFlush(booking);
    }

    @Test
    void create_ValidBooking_ShouldGenerateId() {
        Booking booking = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                getTestItem(), getTestBooker(), BookingStatus.WAITING);
        Booking saved = bookingRepository.saveAndFlush(booking);
        assertNotNull(saved.getId());
    }

    @Test
    void findByBookerId_ShouldReturnSortedByStartDesc() {
        LocalDateTime now = LocalDateTime.now();
        createAndSaveBooking(now.minusDays(3), now.minusDays(2), BookingStatus.APPROVED, getTestItem(), getTestBooker());
        createAndSaveBooking(now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, getTestItem(), getTestBooker());

        Sort sort = Sort.by("start").descending();
        List<Booking> result = bookingRepository.findByBookerId(getTestBooker().getId(), sort);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getStart().isAfter(result.get(1).getStart()));
    }

    @Test
    void findByBookerIdAndEndIsBefore_ShouldReturnPastBookings() {
        LocalDateTime now = LocalDateTime.now();
        Booking past = createAndSaveBooking(now.minusDays(3), now.minusDays(1), BookingStatus.APPROVED, getTestItem(), getTestBooker());
        createAndSaveBooking(now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, getTestItem(), getTestBooker());

        List<Booking> result = bookingRepository.findByBookerIdAndEndIsBefore(
                getTestBooker().getId(), now, Sort.by("start").descending());

        assertEquals(1, result.size());
        assertEquals(past.getId(), result.getFirst().getId());
    }

    @Test
    void findByBookerIdAndActiveOnDate_ShouldReturnCurrentBookings() {
        LocalDateTime now = LocalDateTime.now();
        Booking current = createAndSaveBooking(now.minusHours(1), now.plusHours(1), BookingStatus.APPROVED, getTestItem(), getTestBooker());
        createAndSaveBooking(now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED, getTestItem(), getTestBooker());

        List<Booking> result = bookingRepository.findByBookerIdAndActiveOnDate(
                getTestBooker().getId(), now, Sort.by("start").descending());

        assertEquals(1, result.size());
        assertEquals(current.getId(), result.getFirst().getId());
    }

    @Test
    void findByBookerIdAndStartIsAfter_ShouldReturnFutureBookings() {
        LocalDateTime now = LocalDateTime.now();
        Booking future = createAndSaveBooking(now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, getTestItem(), getTestBooker());
        createAndSaveBooking(now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED, getTestItem(), getTestBooker());

        List<Booking> result = bookingRepository.findByBookerIdAndStartIsAfter(
                getTestBooker().getId(), now, Sort.by("start").descending());

        assertEquals(1, result.size());
        assertEquals(future.getId(), result.getFirst().getId());
    }

    @Test
    void findByBookerIdAndStatusIs_ShouldReturnBookingsWithGivenStatus() {
        LocalDateTime now = LocalDateTime.now();
        createAndSaveBooking(now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, getTestItem(), getTestBooker());
        createAndSaveBooking(now.plusDays(3), now.plusDays(4), BookingStatus.APPROVED, getTestItem(), getTestBooker());

        List<Booking> result = bookingRepository.findByBookerIdAndStatusIs(
                getTestBooker().getId(), BookingStatus.WAITING, Sort.by("start").descending());

        assertEquals(1, result.size());
        assertEquals(BookingStatus.WAITING, result.getFirst().getStatus());
    }

    @Test
    void findByItemOwnerId_ShouldReturnBookingsForOwnerItems() {
        LocalDateTime now = LocalDateTime.now();
        // Создаём второй item через реализацию
        Item item2 = createAndSaveItem("Item2", "Desc2");
        createAndSaveBooking(now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, getTestItem(), getTestBooker());
        createAndSaveBooking(now.plusDays(3), now.plusDays(4), BookingStatus.WAITING, item2, getOtherBooker());

        List<Booking> result = bookingRepository.findByItemOwnerId(getOwnerId(), Sort.by("start").descending());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> b.getItem().getOwnerId().equals(getOwnerId())));
    }

    @Test
    void findByItemOwnerIdAndEndIsBefore_ShouldReturnPastOwnerBookings() {
        LocalDateTime now = LocalDateTime.now();
        Booking past = createAndSaveBooking(now.minusDays(3), now.minusDays(1), BookingStatus.APPROVED, getTestItem(), getTestBooker());
        createAndSaveBooking(now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, getTestItem(), getTestBooker());

        List<Booking> result = bookingRepository.findByItemOwnerIdAndEndIsBefore(
                getOwnerId(), now, Sort.by("start").descending());

        assertEquals(1, result.size());
        assertEquals(past.getId(), result.getFirst().getId());
    }

    @Test
    void findByItemOwnerIdAndActiveOnDate_ShouldReturnCurrentOwnerBookings() {
        LocalDateTime now = LocalDateTime.now();
        Booking current = createAndSaveBooking(now.minusHours(1), now.plusHours(1), BookingStatus.APPROVED, getTestItem(), getTestBooker());
        createAndSaveBooking(now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED, getTestItem(), getTestBooker());

        List<Booking> result = bookingRepository.findByItemOwnerIdAndActiveOnDate(
                getOwnerId(), now, Sort.by("start").descending());

        assertEquals(1, result.size());
        assertEquals(current.getId(), result.getFirst().getId());
    }

    @Test
    void findByItemOwnerIdAndStartIsAfter_ShouldReturnFutureOwnerBookings() {
        LocalDateTime now = LocalDateTime.now();
        Booking future = createAndSaveBooking(now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, getTestItem(), getTestBooker());
        createAndSaveBooking(now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED, getTestItem(), getTestBooker());

        List<Booking> result = bookingRepository.findByItemOwnerIdAndStartIsAfter(
                getOwnerId(), now, Sort.by("start").descending());

        assertEquals(1, result.size());
        assertEquals(future.getId(), result.getFirst().getId());
    }

    @Test
    void findByItemOwnerIdAndStatusIs_ShouldReturnOwnerBookingsWithStatus() {
        LocalDateTime now = LocalDateTime.now();
        createAndSaveBooking(now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, getTestItem(), getTestBooker());
        createAndSaveBooking(now.plusDays(3), now.plusDays(4), BookingStatus.APPROVED, getTestItem(), getOtherBooker());

        List<Booking> result = bookingRepository.findByItemOwnerIdAndStatusIs(
                getOwnerId(), BookingStatus.WAITING, Sort.by("start").descending());

        assertEquals(1, result.size());
        assertEquals(BookingStatus.WAITING, result.getFirst().getStatus());
    }

    @Test
    void findByItemIdWithItem_ShouldLoadItem() {
        LocalDateTime now = LocalDateTime.now();
        createAndSaveBooking(now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, getTestItem(), getTestBooker());

        List<Booking> result = bookingRepository.findByItemIdWithItem(getTestItem().getId());

        assertEquals(1, result.size());
        assertNotNull(result.getFirst().getItem());
        assertEquals(getTestItem().getId(), result.getFirst().getItem().getId());
    }

    @Test
    void findByItemIdsWithItem_ShouldLoadItemsForMultipleIds() {
        LocalDateTime now = LocalDateTime.now();
        Item item2 = createAndSaveItem("Item2", "Desc2");
        createAndSaveBooking(now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, getTestItem(), getTestBooker());
        createAndSaveBooking(now.plusDays(3), now.plusDays(4), BookingStatus.WAITING, item2, getOtherBooker());

        List<Booking> result = bookingRepository.findByItemIdsWithItem(List.of(getTestItem().getId(), item2.getId()));

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> b.getItem() != null));
    }

    @Test
    void findPastApprovedBookingsByItemIdAndBookerId_ShouldReturnMatching() {
        LocalDateTime now = LocalDateTime.now();
        Booking pastApproved = createAndSaveBooking(now.minusDays(3), now.minusDays(1), BookingStatus.APPROVED, getTestItem(), getTestBooker());
        createAndSaveBooking(now.minusDays(5), now.minusDays(4), BookingStatus.WAITING, getTestItem(), getTestBooker());
        createAndSaveBooking(now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED, getTestItem(), getTestBooker());

        List<Booking> result = bookingRepository.findPastApprovedBookingsByItemIdAndBookerId(
                getTestItem().getId(), getTestBooker().getId(), now);

        assertEquals(1, result.size());
        assertEquals(pastApproved.getId(), result.getFirst().getId());
    }

    // Абстрактные методы для создания дополнительных сущностей
    protected abstract Item createAndSaveItem(String name, String description);
}