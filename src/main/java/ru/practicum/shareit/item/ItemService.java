package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {

    ItemDto createItem(long userId, NewItemRequest newItemDto);

    ItemDto patchItem(long userId, long itemId, PatchItemRequest newItem);

    ItemDto getItemById(long userId, long itemId);

    List<ItemWithBookingDatesDto> getAllItems(long userId);

    List<ItemDto> searchItemByText(long userId, String text);

    boolean hasItems(Long userId);

    CommentDto createCommentForItem(Long userId, Long itemId, NewCommentDto commentDto);
}