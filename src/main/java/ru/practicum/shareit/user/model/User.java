package ru.practicum.shareit.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Positive(message = "Ид должен быть больше 0") Long id;
    @Column(nullable = false)
    private @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная почта должна соответствовать формату электронного адреса") String email;
    @Column(nullable = false)
    private @NotBlank(message = "Имя не может быть пустым") String name;
}
