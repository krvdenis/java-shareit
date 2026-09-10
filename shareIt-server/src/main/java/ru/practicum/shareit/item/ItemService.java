package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItem;
import ru.practicum.shareit.item.dto.PatchItem;

import java.util.List;

public interface ItemService {

    ItemDto createItem(Long userId, NewItem newItemDto);

    ItemDto patchItem(Long userId, Long itemId, PatchItem newItem);

    ItemDto getItemById(Long userId, Long itemId);

    List<ItemDto> getAllItems(Long userId);

    List<ItemDto> searchItemByText(Long userId, String text);

    CommentDto createCommentForItem(Long userId, Long itemId, NewCommentDto commentDto);
}