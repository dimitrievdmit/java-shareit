package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.item.dto.comment.CommentResponseDTO;
import ru.practicum.shareit.item.dto.item.*;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.mapper.BookingMapper.mapToBookingInItemDTO;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {

    /**
     * Преобразует доменную модель Item в ItemResponseDTO (с ID).
     */
    public static ItemResponseDTO mapToResponseDTO(Item item) {
        return new ItemResponseDTO(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId()
        );
    }

    /**
     * Преобразует ItemBaseRequestDTO в доменную модель Item.
     */
    public static Item mapToDomain(ItemBaseRequestDTO itemBaseRequestDTO, Long ownerId) {
        return new Item(
                null,
                ownerId,
                itemBaseRequestDTO.name(),
                itemBaseRequestDTO.description(),
                itemBaseRequestDTO.available(),
                itemBaseRequestDTO.requestId()
        );
    }

    /**
     * Обновляет данные объекта Item, создавая новый экземпляр на основе itemUpdate.
     * Сохраняет неизменным id и ownerId.
     * Обновляет только заполненные в запросе поля.
     */
    public static Item updateFromDTO(ItemUpdateDTO itemUpdate, Item item) {
        return new Item(
                item.getId(),
                item.getOwnerId(),
                (itemUpdate.name() != null) ? itemUpdate.name() : item.getName(),
                (itemUpdate.description() != null && !itemUpdate.description().isBlank())
                        ? itemUpdate.description()
                        : item.getDescription(),
                (itemUpdate.available() != null) ? itemUpdate.available() : item.getAvailable(),
                (itemUpdate.requestId() != null) ? itemUpdate.requestId() : item.getRequestId()
        );
    }

    /**
     * Преобразует список доменных моделей Item в список ItemResponseDTO с сортировкой по ID.
     */
    public static List<ItemResponseDTO> mapToResponseDTOList(Collection<Item> items) {
        return items.stream()
                .sorted(Comparator.comparing(Item::getId))
                .map(ItemMapper::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует Item в ItemWithBookingDTO с комментариями и без бронирований
     */
    public static ItemWithBookingDTO mapToDTOWithoutBookings(Item item, List<CommentResponseDTO> commentDtos) {
        return new ItemWithBookingDTO(
                item.getId(),
                item.getOwnerId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId(),
                null,
                null,
                commentDtos
        );
    }

    /**
     * Преобразует Item в ItemWithBookingDTO с комментариями и бронированиями
     */
    public static ItemWithBookingDTO mapToDTOWithBookings(Item item,
                                                          List<Booking> bookings,
                                                          List<CommentResponseDTO> commentDtos) {
        LocalDateTime now = LocalDateTime.now();

        // учитываем только APPROVED бронирования
        List<Booking> approvedBookings = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .toList();

        // lastBooking = последнее завершённое APPROVED
        Booking lastBooking = approvedBookings.stream()
                .filter(b -> b.getEnd().isBefore(now))
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);

        // nextBooking = ближайшее будущее APPROVED
        Booking nextBooking = approvedBookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        return new ItemWithBookingDTO(
                item.getId(),
                item.getOwnerId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId(),
                lastBooking != null ? mapToBookingInItemDTO(lastBooking) : null,
                nextBooking != null ? mapToBookingInItemDTO(nextBooking) : null,
                commentDtos
        );
    }

    public static ItemShortDTO mapToShortDTO(Item item) {
        return new ItemShortDTO(
                item.getId(),
                item.getName(),
                item.getOwnerId()
        );
    }

    public static List<ItemShortDTO> mapToShortDTOList(Collection<Item> items) {
        return items.stream()
                .map(ItemMapper::mapToShortDTO)
                .collect(Collectors.toList());
    }
}