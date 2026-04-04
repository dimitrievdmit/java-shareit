package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;

import static ru.practicum.shareit.validator.Validator.MAX_COMMENT_LENGTH;

@Entity
@Table(name = "comments")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Getter
@Setter
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Positive(message = "Ид должен быть больше 0")
    Long id;

    @NotBlank(message = "Текст отзыва не может быть пустым")
    @Size(max = MAX_COMMENT_LENGTH, message = "Текст отзыва не может быть длиннее {max} символов")
    @Column(nullable = false)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "Комментируемая вещь должна быть указана")
    private Item item;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    @NotNull(message = "Пользователь, который оставляет отзыв должен быть указан")
    private User author;

    @Column(name = "created_date", nullable = false)
    private Instant created;
}
