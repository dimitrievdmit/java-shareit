package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingCreateDTO;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentViewDTO;
import ru.practicum.shareit.item.dto.CommentResponseDTO;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentMapper {

    /**
     * Преобразует доменную модель Comment в CommentResponseDTO,
     * используя заранее загруженную вещь.
     */
    public static CommentResponseDTO mapToResponseDTO(Comment comment, Item item) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getText(),
                ItemMapper.mapToResponseDTO(item),
                UserMapper.mapToResponseDTO(comment.getAuthor()),
                comment.getCreated()
        );
    }

    /**
     * Преобразует проекцию CommentViewDTO в CommentResponseDTO,
     * используя заранее загруженную вещь.
     */
    public static CommentResponseDTO mapToResponseDTO(CommentViewDTO viewDTO, Item item) {
        return new CommentResponseDTO(
                viewDTO.id(),
                viewDTO.text(),
                ItemMapper.mapToResponseDTO(item),
                viewDTO.author(),
                viewDTO.created()
        );
    }

    /**
     * Преобразует доменную модель Comment в CommentResponseDTO,
     * загружая вещь из самого комментария.
     */
    public static CommentResponseDTO mapToResponseDTO(Comment comment) {
        return mapToResponseDTO(comment, comment.getItem());
    }

    /**
     * Преобразует коллекцию Comment в список CommentResponseDTO,
     * загружая вещь из каждого комментария.
     */
    public static List<CommentResponseDTO> mapToResponseDTOList(List<Comment> comments) {
        return comments.stream()
                .map(CommentMapper::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует коллекцию Comment в список CommentResponseDTO,
     * используя заранее загруженную вещь для всех комментариев.
     */
    public static List<CommentResponseDTO> mapToResponseDTOList(List<Comment> comments, Item item) {
        return comments.stream()
                .map(projection -> mapToResponseDTO(projection, item))
                .collect(Collectors.toList());
    }

    /**
     * Преобразует коллекцию CommentViewDTO в список CommentResponseDTO,
     * используя заранее загруженную вещь для всех комментариев.
     */
    public static List<CommentResponseDTO> mapViewToResponseDTOList(List<CommentViewDTO> viewDtos, Item item) {
        return viewDtos.stream()
                .map(dto -> mapToResponseDTO(dto, item))
                .collect(Collectors.toList());
    }

    public static Comment mapCreateToDomain(String text, Item item, User user) {
        Instant created = Instant.now();
        return new Comment(
                null,
                text,
                item,
                user,
                created
        );
    }


}
