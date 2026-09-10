package ru.practicum.shareit.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ResponseItemDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseItemMapper {

    public static ResponseItemDto mapToDto(Item item) {
        ResponseItemDto dto = new ResponseItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        return dto;
    }
}
