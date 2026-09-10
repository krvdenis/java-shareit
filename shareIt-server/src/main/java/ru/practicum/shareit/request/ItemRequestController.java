package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDetailedDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequest;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@Slf4j
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto createItemRequest(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                            @RequestBody NewItemRequest newItemRequest) {
        log.info("Поступил запрос на создание запроса для вещи: {}. Отправил запрос пользователь с ID: {}.",
                newItemRequest, userId);
        return itemRequestService.createItemRequest(userId, newItemRequest);
    }

    @GetMapping
    public List<ItemRequestDetailedDto> getItemRequestsByRequesterId(@RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Поступил запрос на получение всех собственных запросов. Отправил запрос пользователь с ID: {}.",
                userId);
        return itemRequestService.getItemRequestsByRequesterId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getRequestsFromOthers(@RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Поступил запрос на получение всех запросов других пользователей. Отправил запрос пользователь с " +
                        "ID: {}.", userId);
        return itemRequestService.getRequestsFromOthers(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDetailedDto getItemRequestById(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                             @PathVariable Long requestId) {
        log.info("Поступил запрос на получение запроса по ID {}. Отправил запрос пользователь с " +
                "ID: {}.", requestId, userId);
        return itemRequestService.getItemRequestById(userId, requestId);
    }
}