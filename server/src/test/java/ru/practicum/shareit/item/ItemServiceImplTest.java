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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItem;
import ru.practicum.shareit.item.dto.PatchItem;
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

    @BeforeEach
    void setUp() {
        UserDto userDto = new UserDto();
        userDto.setName("Denis");
        userDto.setEmail("denis@mail.ru");

        UserDto createdUser = userService.createUser(userDto);
        userId = createdUser.getId();
    }

    @Test
    void getUserItems_whenInvoked_thenReturnedItems() {
        createItem(userId, "Книга 1", "Описание 1");
        createItem(userId, "Книга 2", "Описание 2");

        List<ItemDto> items = itemService.getUserItems(userId);

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getName()).isEqualTo("Книга 1");
        assertThat(items.get(1).getName()).isEqualTo("Книга 2");
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
    void createCommentForItem_whenInvoked_thenReturnedComment() throws InterruptedException {
        ItemDto itemDto = createItem(userId, "Книга 1", "Описание 1");

        NewBookingRequestDto newBooking = new NewBookingRequestDto(
                itemDto.getId(),
                LocalDateTime.now(),
                LocalDateTime.now().plusNanos(50_000_000)
        );
        BookingDto booking = bookingService.createBookingRequest(userId, newBooking);
        bookingService.updateBookingStatus(userId, booking.getId(), true);

        Thread.sleep(100);

        NewCommentDto newCommentDto = new NewCommentDto("Хорошая книга");
        CommentDto actualComment = itemService.createCommentForItem(userId, itemDto.getId(), newCommentDto);

        assertThat(actualComment.getItemId()).isEqualTo(itemDto.getId());
        assertThat(actualComment.getText()).isEqualTo(newCommentDto.getText());
    }

    @Test
    void patchItem_whenOwnerInvoked_thenReturnedNewItem() {
        ItemDto itemDto = createItem(userId, "Книга 1", "Описание 1");
        PatchItem patchItem = new PatchItem("Книга 4", "Описание 4", false);

        ItemDto actualItem = itemService.patchItem(userId, itemDto.getId(), patchItem);

        assertThat(actualItem.getName()).isEqualTo(patchItem.getName());
        assertThat(actualItem.getDescription()).isEqualTo(patchItem.getDescription());
        assertThat(actualItem.getAvailable()).isEqualTo(patchItem.getAvailable());
        assertThat(actualItem.getId()).isEqualTo(itemDto.getId());
    }

    @Test
    void patchItem_whenNotOwnerInvoked_thenNoAccessExceptionThrown() {
        ItemDto itemDto = createItem(userId, "Книга 1", "Описание 1");
        PatchItem patchItem = new PatchItem("Книга 4", "Описание 4", false);
        long wrongUserId = 25;

        assertThrows(NoAccessException.class, () -> itemService.patchItem(wrongUserId, itemDto.getId(), patchItem));
    }

    @Test
    void getItemById_whenInvoked_thenReturnedItem() {
        ItemDto itemDto = createItem(userId, "Книга 1", "Описание 1");

        ItemDto actualItem = itemService.getItemById(userId, itemDto.getId());

        assertThat(actualItem.getName()).isEqualTo(itemDto.getName());
        assertThat(actualItem.getDescription()).isEqualTo(itemDto.getDescription());
        assertThat(actualItem.getAvailable()).isEqualTo(itemDto.getAvailable());
        assertThat(actualItem.getId()).isEqualTo(itemDto.getId());
    }

    @Test
    void searchItemByText_whenInvoked_thenReturnedItems() {
        createItem(userId, "Книга 1", "Описание 1");
        ItemDto itemDto2 = createItem(userId, "Книга 2", "Описание 2");
        String text = "Описание 2";

        List<ItemDto> items = itemService.searchItemByText(userId, text);

        assertThat(items).hasSize(1);
        assertThat(items.getFirst().getDescription()).isEqualTo(text);
        assertThat(items.getFirst().getName()).isEqualTo(itemDto2.getName());
        assertThat(items.getFirst().getId()).isEqualTo(itemDto2.getId());
    }

    private ItemDto createItem(long ownerId, String name, String description) {
        NewItem newItem = new NewItem(name, description, true, null);
        return itemService.createItem(ownerId, newItem);
    }
}