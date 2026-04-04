package ru.practicum.shareit.booking.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;

@Entity
@Table(name = "bookings")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Getter
@Setter
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Positive(message = "Ид бронирования должен быть больше 0")
    Long id;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Дата и время начала бронирования должна быть указана")
    private Instant start;

    @Column(name = "end_date", nullable = false)
    @NotNull(message = "Дата и время конца бронирования должна быть указана")
    private Instant end;

    // По тестам в ТЗ не существует сценария, когда не нужно выгружать item.id и item.name.
    // В связи с этим, несмотря на рекомендации в уроках, наиболее логичный тип выгрузки здесь EAGER.
    // Иначе, придётся переделать все методы Get на Query с JoinFetch,
    // включая переопределение наследуемых стандартных JPA методов,
    // а также, отказаться от функционала запросных методов.
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = "Бронируемая вещь должна быть указана")
    private Item item;

    // По тестам в ТЗ не существует сценария, когда не нужно выгружать booker.id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "booker_id")
    @NotNull(message = "Пользователь, который осуществляет бронирование должен быть указан")
    private User booker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Статус бронирования должен быть указан")
    private BookingStatus status;


}
