package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDates;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.PatchItemRequest;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemDto createItem(Long userId, NewItemRequest newItemDto) {
        log.debug("Попытка зарегистрировать новую вещь: {}", newItemDto);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        Item item = ItemMapper.mapToItem(newItemDto);
        item.setOwner(user);
        ItemDto itemDto = ItemMapper.mapToItemDto(itemRepository.save(item));
        log.info("Вещь {} успешно добавлена под ID {}", itemDto.getName(), itemDto.getId());
        return itemDto;
    }

    @Override
    @Transactional
    public CommentDto createCommentForItem(Long userId, Long itemId, NewCommentDto newCommentDto) {
        log.debug("Попытка добавить новый отзыв: {}", newCommentDto);
        LocalDateTime now = LocalDateTime.now();
        Comment comment = CommentMapper.mapToComment(newCommentDto);
        comment.setCreated(now);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item с ID %d не найден".formatted(itemId)));

        if (!bookingRepository.existsByBookerIdAndItemIdAndEndTimeLessThanAndStatus(userId, itemId,
                comment.getCreated(), BookingStatus.APPROVED)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Пользователь не может оставить отзыв, так как ранее не арендовал эту вещь."
            );
        }
        comment.setItem(item);
        comment.setAuthor(user);
        CommentDto commentDto = CommentMapper.mapToCommentDto(commentRepository.save(comment));
        log.info("Отзыв {} успешно добавлен под ID {}", commentDto.getText(), commentDto.getId());
        return commentDto;
    }

    @Override
    @Transactional
    public ItemDto patchItem(Long userId, Long itemId, PatchItemRequest newItem) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item с ID %d не найден".formatted(itemId)));

        if (!Objects.equals(userId, item.getOwner().getId())) {
            throw new NoAccessException("Только владелец вещи может внести изменения в её характеристики");
        }
        log.debug("Попытка внести изменения в характеристики вещи {}", newItem);
        ItemMapper.patchItemFields(item, newItem);
        Item patchedItem = itemRepository.save(item);
        log.info("Данные вещи с ID {} успешно обновлены", patchedItem.getId());
        return ItemMapper.mapToItemDto(patchedItem);
    }

    @Override
    public ItemDto getItemById(Long userId, Long itemId) {
        log.debug("Попытка получить вещь: itemId={}", itemId);
        Item item = itemRepository.findByIdWithComments(itemId)
                .orElseThrow(() -> new NotFoundException("Item с ID %d не найден".formatted(itemId)));
        item.getComments();
        log.info("Получен Item с ID {} c списком из {} отзывов", item.getId(), item.getComments().size());

        ItemDto itemDto = ItemMapper.mapToItemDto(item);
        Optional<BookingDates> lastBooking = bookingRepository.findLastApprovedBooking(itemId,
                LocalDateTime.now().minusSeconds(3), BookingStatus.APPROVED);
        Optional<BookingDates> nextBooking = bookingRepository.findNextApprovedBooking(itemId, LocalDateTime.now(),
                BookingStatus.APPROVED);

        itemDto.setLastBooking(lastBooking.orElse(null));
        itemDto.setNextBooking(nextBooking.orElse(null));
        log.info("Отправлена информация о вещи c ID: {}", itemDto.getId());
        return itemDto;
    }

    @Override
    public List<ItemDto> getAllItems(Long userId) {
        log.debug("Попытка получить список всех вещей пользователя с ID:{}", userId);
        LocalDateTime now = LocalDateTime.now();
        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> itemIds = items.stream()
                .map(item -> item.getId())
                .collect(Collectors.toList());
        Map<Long, List<Comment>> commentsMap = commentRepository.findByItemIdWithItem(itemIds).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        Map<Long, List<BookingDates>> groupedByItem = bookingRepository.findAllByItemIdsAndStatus(itemIds,
                        BookingStatus.APPROVED).stream()
                .collect(Collectors.groupingBy(BookingDates::getItemId));

        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            List<Comment> comments = commentsMap.getOrDefault(item.getId(), Collections.emptyList()).stream()
                    .sorted(Comparator.comparing(Comment::getCreated).reversed())
                    .collect(Collectors.toList());

            List<BookingDates> bookings = groupedByItem.getOrDefault(item.getId(), Collections.emptyList());

            BookingDates lastBooking = bookings.stream()
                    .filter(b -> b.getEnd() != null && b.getEnd().isBefore(now))
                    .max(Comparator.comparing(BookingDates::getEnd))
                    .orElse(null);

            BookingDates nextBooking = bookings.stream()
                    .filter(b -> b.getStart() != null && b.getStart().isAfter(now))
                    .min(Comparator.comparing(BookingDates::getStart))
                    .orElse(null);

            ItemDto itemDto = ItemMapper.mapToItemDto(item, comments, lastBooking, nextBooking);
            itemDtos.add(itemDto);
        }
        itemDtos.sort(Comparator.comparing(ItemDto::getId));
        return itemDtos;
    }

    @Override
    public List<ItemDto> searchItemByText(Long userId, String text) {
        log.debug("Попытка найти список вещей по параметру: text={}", text);
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        List<ItemDto> itemsDto = itemRepository.searchByText(text).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
        log.info("Отправлен список из {} вещей", itemsDto.size());
        return itemsDto;
    }
}