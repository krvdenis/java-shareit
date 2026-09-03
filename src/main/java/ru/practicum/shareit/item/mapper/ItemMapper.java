package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDates;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.PatchItemRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {
    public static ItemDto mapToItemDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        List<CommentDto> sortedComments = item.getComments().stream()
                .sorted(Comparator.comparing(Comment::getCreated).reversed())
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());
        dto.setComments(sortedComments);
        return dto;
    }

    public static ItemDto mapToItemDto(Item item, List<Comment> comments, BookingDates lastBooking,
                                       BookingDates nextBooking) {

        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());

        if (comments != null) {
            dto.setComments(comments.stream()
                    .map(CommentMapper::mapToCommentDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setComments(Collections.emptyList());
        }

        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);

        return dto;
    }

    public static Item mapToItem(NewItemRequest newItemRequest) {
        Item item = new Item();
        item.setName(newItemRequest.getName());
        item.setDescription(newItemRequest.getDescription());
        item.setAvailable(newItemRequest.getAvailable());
        return item;
    }

    public static void patchItemFields(Item item, PatchItemRequest patchItemRequest) {
        if (patchItemRequest.hasName()) {
            item.setName(patchItemRequest.getName());
        }
        if (patchItemRequest.hasDescription()) {
            item.setDescription(patchItemRequest.getDescription());
        }
        if (patchItemRequest.hasAvailable()) {
            item.setAvailable(patchItemRequest.getAvailable());
        }
    }
}