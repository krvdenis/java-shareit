package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDetailedDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ItemRequestServiceImplTest {

    @Autowired
    private UserService userService;
    @Autowired
    private ItemRequestService itemRequestService;
    private Long userId1;
    private Long userId2;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        UserDto userDto1 = new UserDto();
        userDto1.setName("Denis");
        userDto1.setEmail("denis@mail.ru");
        UserDto userDto2 = new UserDto();
        userDto2.setName("Irina");
        userDto2.setEmail("irina@mail.ru");

        UserDto createdUser1 = userService.createUser(userDto1);
        userId1 = createdUser1.getId();
        UserDto createdUser2 = userService.createUser(userDto1);
        userId2 = createdUser2.getId();

        NewItemRequest newItemRequest1 = new NewItemRequest("Кулинарная книга");
        NewItemRequest newItemRequest2 = new NewItemRequest("Дрель");
        itemRequestDto = itemRequestService.createItemRequest(userId1, newItemRequest1);
        itemRequestService.createItemRequest(userId1, newItemRequest2);
    }

    @Test
    void getItemRequestsByRequesterId_whenUserExists_thenReturnedItemRequests() {
        List<ItemRequestDetailedDto> itemRequests = itemRequestService.getItemRequestsByRequesterId(userId1);
        itemRequests.sort(Comparator.comparing(ItemRequestDetailedDto::getId));

        assertThat(itemRequests).hasSize(2);
        assertThat(itemRequests.get(0).getDescription()).isEqualTo("Кулинарная книга");
        assertThat(itemRequests.get(1).getDescription()).isEqualTo("Дрель");
        assertThat(itemRequests.get(0).getId()).isNotNull();
        assertThat(itemRequests.get(1).getId()).isNotNull();
    }

    @Test
    void getItemRequestsByRequesterId_whenUserNotExists_thenNotFoundExceptionThrown() {
        long wrongUserId = 25;

       assertThrows(NotFoundException.class, () -> itemRequestService.getItemRequestsByRequesterId(wrongUserId));
    }

    @Test
    void createItemRequest_whenInvoked_thenReturnedItemRequest() {
        NewItemRequest expectedItemRequest = new NewItemRequest("Молоток");
        ItemRequestDto actualItemRequest = itemRequestService.createItemRequest(userId1, expectedItemRequest);
        assertThat(actualItemRequest.getDescription()).isEqualTo(expectedItemRequest.getDescription());
        assertThat(actualItemRequest.getId()).isNotNull();

    }

    @Test
    void getRequestsFromOthers_whenInvoked_thenReturnedItemRequests() {
        NewItemRequest itemRequest = new NewItemRequest("Шуруповёрт");
        ItemRequestDto expectedItemRequest = itemRequestService.createItemRequest(userId2, itemRequest);
        List<ItemRequestDto> itemRequests = itemRequestService.getRequestsFromOthers(userId2);

        assertThat(itemRequests).hasSize(2);
        assertThat(itemRequests.get(0).getId()).isNotEqualTo(expectedItemRequest.getId());
        assertThat(itemRequests.get(1).getId()).isNotEqualTo(expectedItemRequest.getId());
    }

    @Test
    void getItemRequestById_whenUserExists_thenReturnedItemRequest() {
        long itemRequestId = itemRequestDto.getId();
        ItemRequestDetailedDto actualItemRequest = itemRequestService.getItemRequestById(userId1, itemRequestId);

        assertThat(actualItemRequest.getDescription()).isEqualTo(itemRequestDto.getDescription());
        assertThat(actualItemRequest.getId()).isEqualTo(itemRequestDto.getId());
    }

    @Test
    void getItemRequestById_whenUserNotExists_thenNotFoundExceptionThrown() {
        long wrongUserId = 25;
        long itemRequestId = itemRequestDto.getId();

        assertThrows(NotFoundException.class, () -> itemRequestService.getItemRequestById(wrongUserId, itemRequestId));
    }
}