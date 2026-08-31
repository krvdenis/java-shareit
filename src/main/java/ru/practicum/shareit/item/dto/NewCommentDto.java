package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;

@Data
public class NewCommentDto {
    @NotNull
    private String text;

    public static Comment to(NewCommentDto dto) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setCreated(LocalDateTime.now());
        return comment;
    }
}
