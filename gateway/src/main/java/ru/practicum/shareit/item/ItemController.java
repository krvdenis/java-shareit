package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItemRequestDto;
import ru.practicum.shareit.item.dto.PatchItemRequestDto;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(X_SHARER_USER_ID) long userId,
                                             @RequestBody @Valid NewItemRequestDto requestDto) {
        log.info("Creating item {}, userId={}", requestDto, userId);
        return itemClient.createItem(userId, requestDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> patchItem(@RequestHeader(X_SHARER_USER_ID) long userId, @PathVariable long itemId,
                                            @RequestBody @Valid PatchItemRequestDto requestDto) {
        log.info("Patching item ID {}, userId={}", itemId, userId);
        return itemClient.patchItem(userId, itemId, requestDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader(X_SHARER_USER_ID) long userId, @PathVariable long itemId) {
        log.info("Get item {}, userId={}", itemId, userId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("Get items, userId={}", userId);
        return itemClient.getItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchByText(@RequestHeader(X_SHARER_USER_ID) long userId, @RequestParam String text) {
        log.info("Searching items by text={}, userId={}", text, userId);
        return itemClient.searchItemByText(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createCommentForItem(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                       @PathVariable long itemId,
                                                       @RequestBody @Valid NewCommentDto requestDto) {
        log.info("Creating comment {} for item ID{}, userId={}", requestDto, itemId, userId);
        return itemClient.createCommentForItem(userId, itemId, requestDto);
    }

}