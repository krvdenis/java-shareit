package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDetailedDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createItemRequest(long userId, NewItemRequest newItemRequest);

    List<ItemRequestDetailedDto> getItemRequestsByRequesterId(long userId);

    List<ItemRequestDto> getRequestsFromOthers(long userId);

    ItemRequestDetailedDto getItemRequestById(long userId, long itemRequestId);
}