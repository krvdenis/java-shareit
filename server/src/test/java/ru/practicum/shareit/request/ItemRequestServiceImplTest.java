package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.NewItem;
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
    @Autowired
    private ItemService itemService;

    private Long userId1;
    private Long userId2;

    @BeforeEach
    void setUp() {
        UserDto userDto1 = new UserDto();
        userDto1.setName("Denis");
        userDto1.setEmail("denis@mail.ru");
        userId1 = userService.createUser(userDto1).getId();

        UserDto userDto2 = new UserDto();
        userDto2.setName("Irina");
        userDto2.setEmail("irina@mail.ru");
        userId2 = userService.createUser(userDto2).getId();
    }

    @Test
    void getItemRequestsByRequesterId_whenUserExists_thenReturnedItemRequests() {
        createRequest(userId1, "Кулинарная книга");
        createRequest(userId1, "Дрель");

        List<ItemRequestDetailedDto> itemRequests = itemRequestService.getItemRequestsByRequesterId(userId1);
        itemRequests.sort(Comparator.comparing(ItemRequestDetailedDto::getId));

        assertThat(itemRequests).hasSize(2);
        assertThat(itemRequests.get(0).getDescription()).isEqualTo("Кулинарная книга");
        assertThat(itemRequests.get(1).getDescription()).isEqualTo("Дрель");
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
        createRequest(userId1, "Кулинарная книга");
        createRequest(userId1, "Дрель");

        NewItemRequest itemRequest = new NewItemRequest("Шуруповёрт");
        ItemRequestDto expectedItemRequest = itemRequestService.createItemRequest(userId2, itemRequest);
        List<ItemRequestDto> itemRequests = itemRequestService.getRequestsFromOthers(userId2);

        assertThat(itemRequests).hasSize(2);
        assertThat(itemRequests.get(0).getId()).isNotEqualTo(expectedItemRequest.getId());
        assertThat(itemRequests.get(1).getId()).isNotEqualTo(expectedItemRequest.getId());
    }

    @Test
    void getItemRequestById_whenUserExists_thenReturnedItemRequest() {
        ItemRequestDto itemRequestDto = createRequestWithItem(userId1, userId2, "Кулинарная книга");

        ItemRequestDetailedDto actualItemRequest = itemRequestService.getItemRequestById(userId1, itemRequestDto.getId());

        assertThat(actualItemRequest.getDescription()).isEqualTo(itemRequestDto.getDescription());
        assertThat(actualItemRequest.getId()).isEqualTo(itemRequestDto.getId());
    }

    @Test
    void getItemRequestById_whenUserNotExists_thenNotFoundExceptionThrown() {
        long wrongUserId = 25;
        ItemRequestDto itemRequestDto = createRequest(userId1, "Кулинарная книга");

        assertThrows(NotFoundException.class, () -> itemRequestService.getItemRequestById(wrongUserId, itemRequestDto.getId()));
    }

    private ItemRequestDto createRequest(Long requesterId, String description) {
        return itemRequestService.createItemRequest(requesterId, new NewItemRequest(description));
    }

    private ItemRequestDto createRequestWithItem(Long requesterId, Long ownerId, String description) {
        ItemRequestDto request = createRequest(requesterId, description);
        itemService.createItem(ownerId, new NewItem("ads", "adsad", true, request.getId()));
        return request;
    }
}