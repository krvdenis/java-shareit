package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentMapper {

    public static CommentDto mapToCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setItemId(comment.getItem().getId());
        dto.setAuthorName(comment.getAuthor().getName());
        LocalDateTime rawCreated = comment.getCreated();
        ZonedDateTime utcCreated = rawCreated.atZone(ZoneId.of("UTC"));
        ZonedDateTime moscowCreated = utcCreated.withZoneSameInstant(ZoneId.of("Europe/Moscow"));
        dto.setCreated(moscowCreated.toLocalDateTime());
        return dto;
    }

    public static Comment mapToComment(NewCommentDto dto) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        return comment;
    }
}