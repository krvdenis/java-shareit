package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItem;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
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
        NewItem request1 = new NewItem("Книга 1", "Описание 1", true, null);
        NewItem request2 = new NewItem("Книга 2", "Описание 2", true, null);

        itemService.createItem(userId, request1);
        itemService.createItem(userId, request2);

        List<ItemDto> items = itemService.getUserItems(userId);

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getName()).isEqualTo("Книга 1");
        assertThat(items.get(1).getName()).isEqualTo("Книга 2");
        assertThat(items.get(0).getId()).isNotNull();
        assertThat(items.get(1).getId()).isNotNull();
    }
}