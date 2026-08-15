package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto createItem(long userId, ItemDto itemDto) {
        log.debug("Попытка зарегистрировать новую вещь: {}", itemDto);
        User user = UserMapper.mapToUser(userService.getUserById(userId));
        Item item = ItemMapper.mapToItem(itemDto);
        item.setOwner(user);
        itemDto = ItemMapper.mapToItemDto(itemRepository.create(userId, item));
        log.info("Вещь {} успешно добавлена под ID {}", itemDto.getName(), itemDto.getId());
        return itemDto;
    }

    @Override
    public ItemDto patchItem(long userId, long itemId, PatchItemRequest newItem) {
        Item oldItem = itemRepository.getById(userId, itemId)
                .orElseThrow(() -> new NotFoundException("Item с ID %d не найден".formatted(itemId)));
        if (!Objects.equals(userId, oldItem.getOwner().getId())) {
            throw new NoAccessException("Только владелец вещи может внести изменения в её характеристики");
        }
        log.debug("Попытка внести изменения в характеристики вещи {}", newItem);
        Item patchedItem = itemRepository.patch(userId, ItemMapper.patchItemFields(oldItem, newItem));
        log.info("Данные вещи с ID {} успешно обновлены", patchedItem.getId());
        return ItemMapper.mapToItemDto(patchedItem);
    }

    @Override
    public ItemDto getItemById(long userId, long itemId) {
        log.debug("Попытка получить вещь: itemId={}", itemId);
        ItemDto itemDto = itemRepository.getById(userId, itemId)
                .map(ItemMapper::mapToItemDto)
                .orElseThrow(() -> new NotFoundException("Item с ID %d не найден".formatted(itemId)));
        log.info("Отправлена информация о вещи c ID: {}", itemDto.getId());
        return itemDto;
    }

    @Override
    public List<ItemDto> getAllItems(long userId) {
        log.debug("Попытка получить список всех вещей пользователя с ID:{}", userId);
        List<ItemDto> itemsDto = itemRepository.getAll(userId).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
        log.info("Отправлен список из {} вещей", itemsDto.size());
        return itemsDto;
    }

    @Override
    public List<ItemDto> searchItemByText(long userId, String text) {
        log.debug("Попытка найти список вещей по параметру: text={}", text);
        List<ItemDto> itemsDto = itemRepository.searchByTest(userId, text.toLowerCase()).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
        log.info("Отправлен список из {} вещей", itemsDto.size());
        return itemsDto;
    }
}