package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ResponseItemDto;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseItemMapperTest {

    @Test
    void mapToDto_shouldMapAllFieldsCorrectly() {
        User owner = Mockito.mock(User.class);
        Mockito.when(owner.getId()).thenReturn(123L);

        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getId()).thenReturn(456L);
        Mockito.when(item.getName()).thenReturn("Молоток");
        Mockito.when(item.getOwner()).thenReturn(owner);

        ResponseItemDto dto = ResponseItemMapper.mapToDto(item);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(456L);
        assertThat(dto.getName()).isEqualTo("Молоток");
        assertThat(dto.getOwnerId()).isEqualTo(123L);
    }
}