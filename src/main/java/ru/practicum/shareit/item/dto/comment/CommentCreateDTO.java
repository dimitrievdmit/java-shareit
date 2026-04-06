package ru.practicum.shareit.item.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static ru.practicum.shareit.validator.Validator.MAX_COMMENT_LENGTH;


public record CommentCreateDTO(
        @NotBlank(message = "Текст отзыва не может быть пустым")
        @Size(max = MAX_COMMENT_LENGTH, message = "Текст отзыва не может быть длиннее {max} символов")
        String text
) {
}
