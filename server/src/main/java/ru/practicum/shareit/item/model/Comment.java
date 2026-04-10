package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime created;
}