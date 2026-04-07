package ru.practicum.shareit.booking.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingCreateDTO;
import ru.practicum.shareit.booking.dto.BookingInItemDTO;
import ru.practicum.shareit.booking.dto.BookingResponseDTO;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingMapper {

    /**
     * Преобразует доменную модель Booking в BookingResponseDTO.
     */
    public static BookingResponseDTO mapToResponseDTO(Booking booking) {
        return new BookingResponseDTO(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                ItemMapper.mapToResponseDTO(booking.getItem()),
                UserMapper.mapToResponseDTO(booking.getBooker()),
                booking.getStatus()
        );
    }

    /**
     * Преобразует доменную модель Booking в BookingResponseDTO.
     */
    public static BookingInItemDTO mapToBookingInItemDTO(Booking booking) {
        return new BookingInItemDTO(
                booking.getId(),
                booking.getStart(),
                booking.getEnd()
        );
    }

    /**
     * Преобразует BookingCreateDTO в доменную модель Booking.
     * ID не передаётся — будет сгенерирован при создании.
     *
     * @param bookingCreateDTO DTO с данными для создания бронирования
     * @param item             Бронируемая вещь
     * @param booker           Пользователь, который бронирует вещь
     * @return новый экземпляр Booking
     */
    public static Booking mapCreateToDomain(
            BookingCreateDTO bookingCreateDTO,
            Item item,
            User booker
    ) {
        return new Booking(
                null, // ID будет сгенерирован в репозитории
                bookingCreateDTO.start(),
                bookingCreateDTO.end(),
                item,
                booker,
                BookingStatus.WAITING  // значение по умолчанию
        );
    }

    /**
     * Преобразует список доменных моделей в список DTO с сортировкой по ID.
     */
    public static List<BookingResponseDTO> mapToResponseDTOList(Collection<Booking> bookings) {
        return bookings.stream()
                .sorted(Comparator.comparing(Booking::getId))
                .map(BookingMapper::mapToResponseDTO)
                .collect(Collectors.toList());
    }
}
