package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDetailedDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createItemRequest(Long userId, NewItemRequest newItemRequest);

    List<ItemRequestDetailedDto> getAllByRequesterId(Long userId);

    List<ItemRequestDto> getAllOtherItemRequests(Long userId);

    ItemRequestDetailedDto getItemRequestById(Long userId, Long itemRequestId);
}
