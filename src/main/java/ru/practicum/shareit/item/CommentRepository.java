package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c join fetch c.item where c.item.id IN :itemIds order by c.created desc")
    List<Comment> findByItemIdInWithItem(@Param("itemIds") List<Long> itemIds);

    List<Comment> findByItemId(Long itemId);

    List<Comment> findByItemOwnerId(Long itemId);
}