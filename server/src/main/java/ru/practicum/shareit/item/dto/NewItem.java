package ru.practicum.shareit.item.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NewItem {
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}