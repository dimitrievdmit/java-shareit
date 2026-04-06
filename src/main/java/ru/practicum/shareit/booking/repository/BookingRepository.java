package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    // Past
    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime date, Sort sort);

    // Current
    @Query("select book " +
            "from Booking book " +
            "where book.booker.id = ?1 " +
            "and book.start <= ?2 and book.end >= ?2")
    List<Booking> findByBookerIdAndActiveOnDate(Long bookerId, LocalDateTime date, Sort sort);

    // Future
    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime date, Sort sort);

    List<Booking> findByBookerIdAndStatusIs(Long bookerId, BookingStatus bookingStatus, Sort sort);

    @Query("select book " +
            "from Booking as book " +
            "join book.item as it " +
            "where it.ownerId = ?1")
    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    // Past
    @Query("select book " +
            "from Booking as book " +
            "join book.item as it " +
            "where it.ownerId = ?1 " +
            "and book.end < ?2")
    List<Booking> findByItemOwnerIdAndEndIsBefore(Long ownerId, LocalDateTime date, Sort sort);

    // Current
    @Query("select book " +
            "from Booking as book " +
            "join book.item as it " +
            "where it.ownerId = ?1 " +
            "and book.start <= ?2 and book.end >= ?2")
    List<Booking> findByItemOwnerIdAndActiveOnDate(Long ownerId, LocalDateTime date, Sort sort);

    // Future
    @Query("select book " +
            "from Booking as book " +
            "join book.item as it " +
            "where it.ownerId = ?1 " +
            "and book.start > ?2")
    List<Booking> findByItemOwnerIdAndStartIsAfter(Long ownerId, LocalDateTime date, Sort sort);

    @Query("select book " +
            "from Booking as book " +
            "join book.item as it " +
            "where it.ownerId = ?1 " +
            "and book.status = ?2")
    List<Booking> findByItemOwnerIdAndStatusIs(Long ownerId, BookingStatus bookingStatus, Sort sort);


    @Query("SELECT b FROM Booking b JOIN FETCH b.item WHERE b.item.id = ?1")
    List<Booking> findByItemIdWithItem(Long itemId);


    @Query("SELECT b FROM Booking b JOIN FETCH b.item WHERE b.item.id IN :itemIds")
    List<Booking> findByItemIdsWithItem(@Param("itemIds") List<Long> itemIds);

    @Query("""
                SELECT b FROM Booking b
                WHERE b.item.id = :itemId
                AND b.booker.id = :userId
                AND b.end <= :now
                AND b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED
            """)
    List<Booking> findPastApprovedBookingsByItemIdAndBookerId(
            @Param("itemId") Long itemId,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

}