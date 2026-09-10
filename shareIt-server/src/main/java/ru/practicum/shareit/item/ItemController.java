package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItem;
import ru.practicum.shareit.item.dto.PatchItem;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@RequestHeader(X_SHARER_USER_ID) Long userId,
                              @RequestBody NewItem newItemDto) {
        log.info("Поступил запрос на создание вещи: {}. Отправил запрос пользователь с ID: {}.", newItemDto, userId);
        return itemService.createItem(userId, newItemDto);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createCommentForItem(@RequestHeader(X_SHARER_USER_ID) Long userId, @PathVariable Long itemId,
                                           @RequestBody NewCommentDto commentDto) {
        log.info("Поступил запрос на создание отзыва: {}. Отправил запрос пользователь с ID: {}.", commentDto,
                userId);
        return itemService.createCommentForItem(userId, itemId, commentDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patch(@RequestHeader(X_SHARER_USER_ID) Long userId, @PathVariable Long itemId,
                         @RequestBody PatchItem newItem) {
        log.info("Поступил запрос на изменение характеристик вещи: {} с ID {}. Отправил запрос пользователь с ID: {}.",
                newItem, itemId, userId);
        return itemService.patchItem(userId, itemId, newItem);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader(X_SHARER_USER_ID) Long userId, @PathVariable Long itemId) {
        log.info("Поступил запрос на получение вещи с ID: {}. Отправил запрос пользователь с ID: {}.", itemId, userId);
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getAll(@RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Поступил запрос на получение всех вещей. Отправил запрос пользователь с ID: {}.", userId);
        return itemService.getAllItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchByText(@RequestHeader(X_SHARER_USER_ID) Long userId, @RequestParam String text) {
        log.info("Поступил запрос на поиск вещи с text: {}. Отправил запрос пользователь с ID: {}.", text, userId);
        return itemService.searchItemByText(userId, text);
    }
}