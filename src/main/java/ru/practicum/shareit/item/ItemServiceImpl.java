package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDates;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.PatchItemRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ItemDto createItem(Long userId, NewItemRequest newItemDto) {
        log.debug("Попытка зарегистрировать новую вещь: {}", newItemDto);
        User user = UserMapper.mapToUser(userService.getUserById(userId));
        Item item = NewItemRequest.to(newItemDto);
        item.setOwner(user);
        ItemDto itemDto = ItemMapper.mapToItemDto(itemRepository.save(item));
        log.info("Вещь {} успешно добавлена под ID {}", itemDto.getName(), itemDto.getId());
        return itemDto;
    }

    @Transactional
    @Override
    public CommentDto createCommentForItem(Long userId, Long itemId, NewCommentDto newCommentDto) {
        log.debug("Попытка добавить новый отзыв: {}", newCommentDto);
        Comment comment = NewCommentDto.to(newCommentDto);

        if (!bookingRepository.existsByBookerIdAndEndTimeLessThanAndStatus(userId, comment.getCreated(),
                BookingStatus.APPROVED)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Пользователь не может оставить отзыв, так как ранее не арендовал эту вещь."
            );
        }
        User user = UserMapper.mapToUser(userService.getUserById(userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item с ID %d не найден".formatted(itemId)));

        comment.setItem(item);
        comment.setAuthor(user);
        CommentDto commentDto = CommentDto.from(commentRepository.save(comment));
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
        item.getComments().sort(Comparator.comparing(Comment::getCreated).reversed());
        log.info("Получен Item с ID {} c списком из {} отзывов", item.getId(), item.getComments().size());

        ItemDto itemDto = ItemMapper.mapToItemDto(item);
        List<Object[]> result = itemRepository.findLastAndNextBookingDates(itemId, LocalDateTime.now());
        if (!result.isEmpty()) {
            Object[] bookingDates = result.get(0);
            if (bookingDates[0] != null) {
                Timestamp tsLast = (Timestamp) bookingDates[0];
                itemDto.setLastBooking(tsLast.toLocalDateTime());
            } else {
                itemDto.setLastBooking(null);
            }

            if (bookingDates[1] != null) {
                Timestamp tsNext = (Timestamp) bookingDates[1];
                itemDto.setNextBooking(tsNext.toLocalDateTime());
            } else {
                itemDto.setNextBooking(null);
            }
        }
        log.info("Отправлена информация о вещи c ID: {}", itemDto.getId());
        return itemDto;
    }

    @Override
    public List<ItemDto> getAllItems(Long userId) {
        log.debug("Попытка получить список всех вещей пользователя с ID:{}", userId);
        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> itemIds = items.stream()
                .map(item -> item.getId())
                .collect(Collectors.toList());
        List<ItemWithBookingDates> itemsWithBookingDates = itemRepository.findWithBookingDates(itemIds);
        Map<Long, List<Comment>> commentsMap = commentRepository.findByItemIdInWithItem(itemIds).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        List<ItemDto> itemDtos = itemsWithBookingDates.stream()
                .map(itemWithDates -> {
                    ItemDto dto = new ItemDto();
                    dto.setId(itemWithDates.getId());
                    dto.setName(itemWithDates.getName());
                    dto.setDescription(itemWithDates.getDescription());
                    dto.setAvailable(itemWithDates.getAvailable());
                    dto.setLastBooking(itemWithDates.getLastBooking());
                    dto.setNextBooking(itemWithDates.getNextBooking());

                    List<Comment> commentList = commentsMap.get(itemWithDates.getId());

                    if (commentList != null && !commentList.isEmpty()) {
                        dto.setComments(new ArrayList<>(commentList.stream()
                                .map(CommentDto::from)
                                .collect(Collectors.toList())));
                    } else {
                        dto.setComments(Collections.emptyList());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        log.info("Отправлен список из {} вещей", itemDtos.size());
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

    @Override
    public boolean hasItems(Long userId) {
        log.debug("Проверка на наличии хотя бы одной вещи у пользователя с ID {}", userId);
        return itemRepository.existsByOwnerId(userId);
    }
}