package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BookItemRequestDto {
    private long itemId;
    @NotNull(message = "Дата начала бронирования должна быть заполнена")
    @FutureOrPresent
    private LocalDateTime start;
    @NotNull(message = "Дата конца бронирования должна быть заполнена")
    @Future
    private LocalDateTime end;
}
