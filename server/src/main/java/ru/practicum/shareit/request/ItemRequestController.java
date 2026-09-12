package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDetailedDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequest;

import java.util.List;

import static ru.practicum.shareit.common.ApiHeaders.X_SHARER_USER_ID;

@RestController
@Slf4j
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto createItemRequest(@RequestHeader(X_SHARER_USER_ID) long userId,
                                            @RequestBody NewItemRequest newItemRequest) {
        log.info("Поступил запрос на создание запроса для вещи: {}. Отправил запрос пользователь с ID: {}.",
                newItemRequest, userId);
        return itemRequestService.createItemRequest(userId, newItemRequest);
    }

    @GetMapping
    public List<ItemRequestDetailedDto> getItemRequestsByRequesterId(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("Поступил запрос на получение всех собственных запросов. Отправил запрос пользователь с ID: {}.",
                userId);
        return itemRequestService.getItemRequestsByRequesterId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getRequestsFromOthers(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("Поступил запрос на получение всех запросов других пользователей. Отправил запрос пользователь с " +
                "ID: {}.", userId);
        return itemRequestService.getRequestsFromOthers(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDetailedDto getItemRequestById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                     @PathVariable long requestId) {
        log.info("Поступил запрос на получение запроса по ID {}. Отправил запрос пользователь с " +
                "ID: {}.", requestId, userId);
        return itemRequestService.getItemRequestById(userId, requestId);
    }
}