package ru.practicum.shareit.request;

import jakarta.validation.Valid;
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
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id"; //поменять на внешнюю конфигурацию для всех контроллеров
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto createItemRequest(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                            @RequestBody @Valid NewItemRequest newItemRequest) {
        log.info("Поступил запрос на создание запроса для вещи: {}. Отправил запрос пользователь с ID: {}.",
                newItemRequest, userId);
        return itemRequestService.createItemRequest(userId, newItemRequest);
    }

    @GetMapping
    public List<ItemRequestDetailedDto> getAllOwnItemRequests(@RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Поступил запрос на получение всех собственных запросов. Отправил запрос пользователь с ID: {}.",
                userId);
        return itemRequestService.getAllByRequesterId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllOtherItemRequests(@RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Поступил запрос на получение всех запросов других пользователей. Отправил запрос пользователь с " +
                        "ID: {}.", userId);
        return itemRequestService.getAllOtherItemRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDetailedDto getItemRequestById(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                             @PathVariable Long requestId) {
        return itemRequestService.getItemRequestById(userId, requestId);
    }
}