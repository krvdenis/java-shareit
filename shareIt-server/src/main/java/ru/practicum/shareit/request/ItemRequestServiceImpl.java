package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDetailedDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequest;
import ru.practicum.shareit.request.dto.ResponseItemDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.mapper.ResponseItemMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public ItemRequestDto createItemRequest(long userId, NewItemRequest newItemRequest) {
        log.debug("Попытка создать новый запрос на вещь: {}", newItemRequest);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID %d не найден".formatted(userId)));
        ItemRequest itemRequest = ItemRequestMapper.mapToItem(newItemRequest);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequester(user);
        ItemRequestDto dto = ItemRequestMapper.mapToDto(itemRequestRepository.save(itemRequest));
        log.info("Запрос на вещь {} успешно добавлен под ID {}", dto.getDescription(), dto.getId());
        return dto;
    }

    @Override
    public List<ItemRequestDetailedDto> getItemRequestsByRequesterId(long requesterId) {
        if (!userRepository.existsById(requesterId)) {
            throw new NotFoundException("Пользователь с ID %d не найден".formatted(requesterId));
        }

        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdWithOffers(requesterId);
        List<ItemRequestDetailedDto> dtos = itemRequests.stream()
                .map(req -> {
                    List<ResponseItemDto> responses = req.getOfferedItems().stream()
                            .map(ResponseItemMapper::mapToDto)
                            .collect(Collectors.toList());
                    return ItemRequestMapper.mapToDto(req, responses);
                })
                .collect(Collectors.toList());
        return dtos;
    }

    @Override
    public List<ItemRequestDto> getRequestsFromOthers(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID %d не найден".formatted(userId));
        }
        List<ItemRequest> itemRequests = itemRequestRepository.findAllOtherItemRequests(userId);
        return itemRequests.stream()
                .map(ItemRequestMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDetailedDto getItemRequestById(long userId, long itemRequestId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID %d не найден".formatted(userId));
        }
        ItemRequest itemRequest = itemRequestRepository.findAllByIdWithOffers(itemRequestId)
                .orElseThrow(() -> new NotFoundException("Запрос с ID %d не найден".formatted(itemRequestId)));
        List<ResponseItemDto> responses = itemRequest.getOfferedItems().stream()
                .map(ResponseItemMapper::mapToDto)
                .collect(Collectors.toList());
        ItemRequestDetailedDto dto = ItemRequestMapper.mapToDto(itemRequest, responses);
        return dto;
    }
}