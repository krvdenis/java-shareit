package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewCommentDto {
    @NotNull
    private String text;
}