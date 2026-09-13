package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequestDto;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;
    private long userId;
    private NewItem newItem1;
    private NewItem newItem2;
    private ItemDto itemDto1;
    private ItemDto itemDto2;

    @BeforeEach
    void setUp() throws InterruptedException {
        UserDto userDto = new UserDto();
        userDto.setName("Denis");
        userDto.setEmail("denis@mail.ru");

        UserDto createdUser = userService.createUser(userDto);
        userId = createdUser.getId();
        newItem1 = new NewItem("Книга 1", "Описание 1", true, null);
        newItem2 = new NewItem("Книга 2", "Описание 2", true, null);
        itemDto1 = itemService.createItem(userId, newItem1);
        itemDto2 = itemService.createItem(userId, newItem2);
        NewBookingRequestDto newBooking1 = new NewBookingRequestDto(itemDto1.getId(), LocalDateTime.now(),
                LocalDateTime.now().plusSeconds(1));
        BookingDto booking = bookingService.createBookingRequest(userId, newBooking1);
        bookingService.updateBookingStatus(userId, booking.getId(), true);
        Thread.sleep(2000);
    }

    @Test
    void getUserItems_whenInvoked_thenReturnedItems() {
        List<ItemDto> items = itemService.getUserItems(userId);

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getName()).isEqualTo(newItem1.getName());
        assertThat(items.get(1).getName()).isEqualTo(newItem2.getName());
        assertThat(items.get(0).getId()).isNotNull();
        assertThat(items.get(1).getId()).isNotNull();
    }

    @Test
    void createItem_whenInvoked_thenReturnedItem() {
        NewItem newItem = new NewItem("Книга 3", "Описание 3", true, null);

        ItemDto actualItem = itemService.createItem(userId, newItem);

        assertThat(actualItem.getName()).isEqualTo(newItem.getName());
        assertThat(actualItem.getId()).isNotNull();
    }

    @Test
    void createCommentForItem_whenInvoked_thenReturnedComment() {
        NewCommentDto newCommentDto = new NewCommentDto("Хорошая книга");

        CommentDto actualComment = itemService.createCommentForItem(userId, itemDto1.getId(), newCommentDto);

        assertThat(actualComment.getItemId()).isEqualTo(itemDto1.getId());
        assertThat(actualComment.getText()).isEqualTo(newCommentDto.getText());
    }

    @Test
    void patchItem_whenOwnerInvoked_thenReturnedNewItem() {
        PatchItem patchItem = new PatchItem("Книга 4", "Описание 4", false);

        ItemDto actualItem = itemService.patchItem(userId, itemDto1.getId(), patchItem);

        assertThat(actualItem.getName()).isEqualTo(patchItem.getName());
        assertThat(actualItem.getDescription()).isEqualTo(patchItem.getDescription());
        assertThat(actualItem.getAvailable()).isEqualTo(patchItem.getAvailable());
        assertThat(actualItem.getId()).isEqualTo(itemDto1.getId());
    }

    @Test
    void patchItem_whenNotOwnerInvoked_thenNoAccessExceptionThrown() {
        PatchItem patchItem = new PatchItem("Книга 4", "Описание 4", false);
        long wrongUserId = 25;

        assertThrows(NoAccessException.class, () -> itemService.patchItem(wrongUserId, itemDto1.getId(), patchItem));
    }

    @Test
    void getItemById_whenInvoked_thenReturnedItem() {
        long itemId = itemDto1.getId();

        ItemDto actualItem = itemService.getItemById(userId, itemId);

        assertThat(actualItem.getName()).isEqualTo(itemDto1.getName());
        assertThat(actualItem.getDescription()).isEqualTo(itemDto1.getDescription());
        assertThat(actualItem.getAvailable()).isEqualTo(itemDto1.getAvailable());
        assertThat(actualItem.getId()).isEqualTo(itemDto1.getId());
    }

    @Test
    void searchItemByText_whenInvoked_thenReturnedItems() {
        String text = "Описание 2";

        List<ItemDto> items = itemService.searchItemByText(userId, text);

        assertThat(items).hasSize(1);
        assertThat(items.getFirst().getDescription()).isEqualTo(text);
        assertThat(items.getFirst().getName()).isEqualTo(itemDto2.getName());
        assertThat(items.getFirst().getId()).isEqualTo(itemDto2.getId());
    }
}