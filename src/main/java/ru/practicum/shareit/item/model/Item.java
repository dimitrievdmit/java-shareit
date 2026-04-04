package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import static ru.practicum.shareit.validator.Validator.MAX_DESCRIPTION_LENGTH;


@Entity
@Table(name = "items")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Getter
@Setter
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Positive(message = "Ид должен быть больше 0") Long id;
    @Column(name = "owner_id", nullable = false)
    private @NotNull(message = "Ид владельца не может быть пустым")
    @Positive(message = "Ид владельца должен быть больше 0") Long ownerId;
    @Column(nullable = false)
    private @NotBlank(message = "Название не может быть пустым") String name;
    @Column(nullable = false)
    private @NotBlank(message = "Описание не может быть пустым")
    @Size(max = MAX_DESCRIPTION_LENGTH, message = "Описание не может быть длиннее {max} символов") String description;
    @Column(name = "is_available", nullable = false)
    private @NotNull(message = "Флаг доступности для бронирования не может быть пустым") Boolean available;
    @Column(name = "request_id")
    private @Positive(message = "Ид запроса должен быть больше 0") Long requestId;
}
