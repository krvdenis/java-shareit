package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItem;
import ru.practicum.shareit.item.dto.PatchItem;

import java.util.List;

public interface ItemService {

    ItemDto createItem(long userId, NewItem newItemDto);

    ItemDto patchItem(long userId, long itemId, PatchItem newItem);

    ItemDto getItemById(long userId, long itemId);

    List<ItemDto> getUserItems(long userId);

    List<ItemDto> searchItemByText(long userId, String text);

    CommentDto createCommentForItem(long userId, long itemId, NewCommentDto commentDto);
}