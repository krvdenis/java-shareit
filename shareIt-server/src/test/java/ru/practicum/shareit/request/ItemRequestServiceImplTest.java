package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDetailedDto;
import ru.practicum.shareit.request.dto.NewItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ItemRequestServiceImplTest {

    @Autowired
    private UserService userService;
    @Autowired
    private ItemRequestService itemRequestService;
    private Long userId;

    @BeforeEach
    void setUp() {
        UserDto userDto = new UserDto();
        userDto.setName("Denis");
        userDto.setEmail("denis@mail.ru");

        UserDto createdUser = userService.createUser(userDto);
        userId = createdUser.getId();
    }

    @Test
    void getItemRequestsByRequesterId_whenInvoked_thenReturnedItemRequests() {
        NewItemRequest newItemRequest1 = new NewItemRequest("Кулинарная книга");
        NewItemRequest newItemRequest2 = new NewItemRequest("Дрель");

        itemRequestService.createItemRequest(userId, newItemRequest1);
        itemRequestService.createItemRequest(userId, newItemRequest2);

        List<ItemRequestDetailedDto> itemRequests = itemRequestService.getItemRequestsByRequesterId(userId);
        itemRequests.sort(Comparator.comparing(ItemRequestDetailedDto::getId));

        assertThat(itemRequests).hasSize(2);
        assertThat(itemRequests.get(0).getDescription()).isEqualTo("Кулинарная книга");
        assertThat(itemRequests.get(1).getDescription()).isEqualTo("Дрель");
        assertThat(itemRequests.get(0).getId()).isNotNull();
        assertThat(itemRequests.get(1).getId()).isNotNull();
    }
}