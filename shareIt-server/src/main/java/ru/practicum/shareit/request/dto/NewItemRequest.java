package ru.practicum.shareit.request.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NewItemRequest {
    private String description;
}
