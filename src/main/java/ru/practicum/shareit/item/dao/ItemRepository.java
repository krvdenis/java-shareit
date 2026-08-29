package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    Item create(long userId, Item item);

    Item patch(long userId, Item item);

    Optional<Item> getById(long userId, long itemId);

    List<Item> getAll(long userId);

    List<Item> searchByTest(long userId, String text);
}