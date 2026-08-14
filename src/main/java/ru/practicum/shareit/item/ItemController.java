package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@Slf4j
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    public static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader(X_SHARER_USER_ID) long userId, @RequestBody @Valid ItemDto itemDto) {
        log.info("Поступил запрос на создание вещи: {}. Отправил запрос пользователь с ID: {}.", itemDto, userId);
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patch(@RequestHeader(X_SHARER_USER_ID) long userId, @PathVariable long itemId,
                         @RequestBody @Valid PatchItemRequest newItem) {
        log.info("Поступил запрос на изменение характеристик вещи: {} с ID {}. Отправил запрос пользователь с ID: {}.",
                newItem, itemId, userId);
        return itemService.patchItem(userId, itemId, newItem);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader(X_SHARER_USER_ID) long userId, @PathVariable long itemId) {
        log.info("Поступил запрос на получение вещи с ID: {}. Отправил запрос пользователь с ID: {}.", itemId, userId);
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getAll(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("Поступил запрос на получение всех вещей. Отправил запрос пользователь с ID: {}.", userId);
        return itemService.getAllItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchByText(@RequestHeader(X_SHARER_USER_ID) long userId, @RequestParam String text) {
        log.info("Поступил запрос на поиск вещи с text: {}. Отправил запрос пользователь с ID: {}.", text, userId);
        return itemService.searchItemByText(userId, text);
    }
}