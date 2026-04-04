package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.CommentViewDTO;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.item.id = :itemId")
    List<Comment> findByItemIdWithAuthor(@Param("itemId") Long itemId);

    @Query("""
                SELECT new ru.practicum.shareit.item.dto.CommentViewDTO(
                    c.id,
                    c.text,
                    c.item.id,
                    new ru.practicum.shareit.user.dto.UserResponseDTO(
                        c.author.id,
                        c.author.name,
                        c.author.email
                    ),
                    c.created
                )
                FROM Comment c
                WHERE c.item.id IN :itemIds
            """)
    List<CommentViewDTO> findByItemIdsWithAuthorAndItemId(@Param("itemIds") List<Long> itemIds);

}

