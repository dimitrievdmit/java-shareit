package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.comment.CommentResponseDTO;
import ru.practicum.shareit.item.dto.comment.CommentViewDTO;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
                SELECT new ru.practicum.shareit.item.dto.comment.CommentResponseDTO(
                    c.id,
                    c.text,
                    c.author.name,
                    c.created
                )
                FROM Comment c
                WHERE c.item.id IN ?1
            """)
    List<CommentResponseDTO> findByItemIdWithAuthor(Long itemId);

    @Query("""
                SELECT new ru.practicum.shareit.item.dto.comment.CommentViewDTO(
                    c.id,
                    c.text,
                    c.item.id,
                    c.author.name,
                    c.created
                )
                FROM Comment c
                WHERE c.item.id IN :itemIds
            """)
    List<CommentViewDTO> findByItemIdsWithAuthorNameAndItemId(@Param("itemIds") List<Long> itemIds);

}

