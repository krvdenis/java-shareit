package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;

import java.util.List;

public interface ItemService {

    ItemDto createItem(long userId, ItemDto itemDto);

    ItemDto patchItem(long userId, long itemId, PatchItemRequest newItem);

    ItemDto getItemById(long userId, long itemId);

    List<ItemDto> getAllItems(long userId);

    List<ItemDto> searchItemByText(long userId, String text);
}