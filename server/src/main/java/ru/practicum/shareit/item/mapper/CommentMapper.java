package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.comment.CommentResponseDTO;
import ru.practicum.shareit.item.dto.comment.CommentViewDTO;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentMapper {

    /**
     * Преобразует доменную модель Comment в CommentResponseDTO
     */
    public static CommentResponseDTO mapToResponseDTO(Comment comment) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }

    /**
     * Преобразует проекцию CommentViewDTO в CommentResponseDTO
     */
    public static CommentResponseDTO mapViewToResponseDTO(CommentViewDTO viewDTO) {
        return new CommentResponseDTO(
                viewDTO.id(),
                viewDTO.text(),
                viewDTO.authorName(),
                viewDTO.created()
        );
    }

    /**
     * Преобразует коллекцию Comment в список CommentResponseDTO
     */
    public static List<CommentResponseDTO> mapToResponseDTOList(List<Comment> comments) {
        return comments.stream()
                .map(CommentMapper::mapToResponseDTO)
                .toList();
    }

    /**
     * Преобразует коллекцию CommentViewDTO в список CommentResponseDTO
     */
    public static List<CommentResponseDTO> mapViewToResponseDTOList(List<CommentViewDTO> viewDtos) {
        return viewDtos.stream()
                .map(CommentMapper::mapViewToResponseDTO)
                .toList();
    }

    public static Comment mapCreateToDomain(String text, Item item, User user) {
        LocalDateTime created = LocalDateTime.now();
        return new Comment(
                null,
                text,
                item,
                user,
                created
        );
    }
}