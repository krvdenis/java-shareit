package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import static ru.practicum.shareit.common.ApiHeaders.X_SHARER_USER_ID;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                    @RequestBody @Valid NewItemRequestDto requestDto) {
        log.info("Creating itemRequest {}, userId={}", requestDto, userId);
        return itemRequestClient.createItemRequest(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequestsByRequesterId(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("Get itemRequests by requesterId={}", userId);
        return itemRequestClient.getItemRequestsByRequesterId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getRequestsFromOthers(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("Get itemRequests from others, userId={}", userId);
        return itemRequestClient.getRequestsFromOthers(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequest(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                 @PathVariable long requestId) {
        log.info("Get request {}, userId={}", requestId, userId);
        return itemRequestClient.getItemRequest(userId, requestId);
    }
}