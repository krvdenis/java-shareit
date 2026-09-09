package ru.practicum.shareit.request.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ItemRequestDetailedDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ResponseItemDto> items = new ArrayList<>();
}
