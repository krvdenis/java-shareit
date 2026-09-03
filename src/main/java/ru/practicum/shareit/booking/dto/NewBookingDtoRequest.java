package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class NewBookingDtoRequest {
    private Long itemId;
    @NotNull(message = "Дата начала бронирования должна быть заполнена")
    @FutureOrPresent
    private LocalDateTime start;
    @NotNull(message = "Дата конца бронирования должна быть заполнена")
    @Future
    private LocalDateTime end;
}