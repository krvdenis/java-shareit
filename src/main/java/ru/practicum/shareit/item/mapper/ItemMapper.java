package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {
    public static ItemDto mapToItemDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setComments(new ArrayList<>(item.getComments().stream()
                .map(CommentDto::from)
                .collect(Collectors.toList())));
        return dto;
    }

    public static Item mapToItem(ItemDto itemDto) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        return item;
    }

    public static void patchItemFields(Item item, PatchItemRequest patchItemRequest) {
        if (patchItemRequest.hasName()) {
            item.setName(patchItemRequest.getName());
        }
        if (patchItemRequest.hasDescription()) {
            item.setDescription(patchItemRequest.getDescription());
        }
        if (patchItemRequest.hasAvailable()) {
            item.setAvailable(patchItemRequest.getAvailable());
        }
    }
}