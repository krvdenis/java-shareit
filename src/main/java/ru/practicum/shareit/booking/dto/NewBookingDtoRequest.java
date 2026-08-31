package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;

@Getter @Setter @ToString
public class NewBookingDtoRequest {
    @NotNull
    private LocalDateTime start;
    @NotNull
    private LocalDateTime end;
    private Long itemId;

    public static Booking to(NewBookingDtoRequest dtoRequest) {
        Booking booking =  new Booking();
        booking.setStartTime(dtoRequest.getStart());
        booking.setEndTime(dtoRequest.getEnd());
        return booking;
    }
}
