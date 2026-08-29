package ru.practicum.shareit.item.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class InMemoryItemRepositoryImpl implements ItemRepository {
    private final AtomicLong nextId = new AtomicLong(1);
    private final Map<Long, List<Item>> itemOfUsers = new HashMap<>();
    private final Map<Long, Item> itemsById = new HashMap<>();

    @Override
    public Item create(long userId, Item item) {
        item.setId(nextId.getAndIncrement());
        itemsById.put(item.getId(), item);
        itemOfUsers.computeIfAbsent(userId, k -> new ArrayList<>()).add(item);
        return item;
    }

    @Override
    public Item patch(long userId, Item newItem) {
        Item oldItem = itemOfUsers.get(userId).stream()
                .filter(item -> Objects.equals(newItem.getId(), item.getId()))
                .findAny()
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + newItem.getId()
                        + " не найдена у пользователя"));

        if (newItem.getName() != null && !newItem.getName().equals(oldItem.getName())) {
            oldItem.setName(newItem.getName());
        }
        if (newItem.getDescription() != null && !newItem.getDescription().equals(oldItem.getDescription())) {
            oldItem.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null && !newItem.getAvailable().equals(oldItem.getAvailable())) {
            oldItem.setAvailable(newItem.getAvailable());
        }
        return oldItem;
    }

    @Override
    public Optional<Item> getById(long userId, long itemId) {
        return Optional.ofNullable(itemsById.get(itemId));
    }

    @Override
    public List<Item> getAll(long userId) {
        return itemOfUsers.getOrDefault(userId, Collections.emptyList());
    }

    @Override
    public List<Item> searchByTest(long userId, String text) {
        log.info("userId = {}", userId);
        log.info("text = {}", text);
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemsById.values().stream()
                .filter(item -> item.getAvailable()
                        && (item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase())))
                .peek(item -> System.out.printf("itemOwnerId = %d", item.getOwner().getId()))
                .collect(Collectors.toList());
    }
}