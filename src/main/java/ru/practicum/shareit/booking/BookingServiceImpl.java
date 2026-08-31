package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDtoRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BookingConflictException;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.State.REJECTED;
import static ru.practicum.shareit.booking.State.WAITING;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository repository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    @Transactional
    public BookingDto createBookingRequest(Long userId, NewBookingDtoRequest dtoRequest) {
        log.debug("Попытка оформить новое бронирование: {}", dtoRequest);
        User user = UserMapper.mapToUser(userService.getUserById(userId));
        Item item = ItemMapper.mapToItem(itemService.getItemById(userId, dtoRequest.getItemId()));
        isValid(dtoRequest);
        if (!item.getAvailable()) {
            throw new BookingConflictException("Данная вещь недоступна для бронирования");
        }

        Booking booking = NewBookingDtoRequest.to(dtoRequest);
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(user);
        booking.setItem(item);
        BookingDto bookingDto = BookingDto.from(repository.save(booking));
        log.info("Бронирование успешно зарегистрировано под ID {}", bookingDto.getId());
        return bookingDto;
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        log.debug("Попытка получить информацию о бронировании с ID: {}", bookingId);
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID %d не найден".formatted(bookingId)));

        if (isOwner(userId, booking) || isBooker(userId, booking)) {
            BookingDto bookingDto = BookingDto.from(booking);
            log.info("Отправлена информация бронировании вещи c ID: {}", bookingDto.getId());
            return bookingDto;
        } else {
            throw new NoAccessException("Только владелец вещи или автор бронирования может получить данные о" +
                    " конкретном бронировании");
        }
    }

    @Override
    public List<BookingDto> getBookingsByBookerId(Long userId, String state) {
        log.debug("Попытка получить список всех бронирований пользователя с ID:{}", userId);
        State stateStatus = State.from(state);
        List<Booking> bookings;

        if (stateStatus == null) {
            throw new ValidationException("Неверный параметр запроса");
        }

        switch (stateStatus) {
            case ALL -> bookings = repository.findByBookerIdOrderByStartTimeDesc(userId);
            case CURRENT -> bookings = repository.findCurrentBookingsByBookerId(userId);
            case PAST -> bookings = repository.findPastByBookerId(userId);
            case FUTURE -> bookings = repository.findFutureByBookerId(userId);
            case WAITING -> bookings = repository.findByBookerIdAndStatusOrderByStartTimeDesc(userId, WAITING);
            case REJECTED -> bookings = repository.findByBookerIdAndStatusOrderByStartTimeDesc(userId, REJECTED);
            default -> throw new ValidationException("Обработан неизвестный статус: " + stateStatus);
        }

        List<BookingDto> bookingDtos = bookings.stream()
                .map(BookingDto::from)
                .collect(Collectors.toList());
        log.info("Отправлен список из {} сущностей бронирования", bookingDtos.size());
        return bookingDtos;
    }

    @Override
    public List<BookingDto> getBookingsByOwnerId(Long ownerId, String state) {
        log.debug("Попытка получить список всех бронирований у пользователя с ID:{}", ownerId);
        if (!itemService.hasItems(ownerId)) {
            throw new NotFoundException("За вами не закреплено ни одной вещи");
        }

        State stateStatus = State.from(state);
        List<Booking> bookings;
        if (stateStatus == null) {
            throw new ValidationException("Неверный параметр запроса");
        }

        switch (stateStatus) {
            case ALL -> bookings = repository.findByItemOwnerIdOrderByStartTimeDesc(ownerId);
            case CURRENT -> bookings = repository.findCurrentBookingsByOwnerId(ownerId);
            case PAST -> bookings = repository.findPastByOwnerId(ownerId);
            case FUTURE -> bookings = repository.findFutureByOwnerId(ownerId);
            case WAITING -> bookings = repository.findByItemOwnerIdAndStatusOrderByStartTimeDesc(ownerId, WAITING);
            case REJECTED -> bookings = repository.findByItemOwnerIdAndStatusOrderByStartTimeDesc(ownerId, REJECTED);
            default -> throw new ValidationException("Обработан неизвестный статус: " + stateStatus);
        }

        List<BookingDto> bookingDtos = bookings.stream()
                .map(BookingDto::from)
                .collect(Collectors.toList());
        log.info("Отправлен список из {} сущностей бронирования", bookingDtos.size());
        return bookingDtos;
    }

    @Override
    @Transactional
    public BookingDto approveBookingStatus(Long userId, Long bookingId) {
        log.debug("Попытка подтвердить бронирование с ID {} владельцем с ID {}", bookingId, userId);
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID %d не найден".formatted(bookingId)));
        if (isOwner(userId, booking)) {
            booking.setStatus(BookingStatus.APPROVED);
            BookingDto bookingDto = BookingDto.from(repository.save(booking));
            log.info("Подтверждение бронирования завершено");
            return bookingDto;
        } else {
            throw new NoAccessException("Только владелец вещи может подтверждать бронирование");
        }
    }

    @Override
    @Transactional
    public BookingDto rejectBookingStatus(Long userId, Long bookingId) {
        log.debug("Попытка отклонить бронирование с ID {} владельцем с ID {}", bookingId, userId);
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID %d не найден".formatted(bookingId)));
        if (isOwner(userId, booking)) {
            booking.setStatus(BookingStatus.REJECTED);
            BookingDto bookingDto = BookingDto.from(repository.save(booking));
            log.info("Отклонение бронирования завершено");
            return bookingDto;
        } else {
            throw new NoAccessException("Только владелец вещи может подтверждать бронирование");
        }
    }

    private boolean isOwner(Long userId, Booking booking) {
        log.debug("Проверяется, является ли пользователь с ID {} собственником", userId);
        return booking.getItem().getOwner().getId().equals(userId);
    }

    private boolean isBooker(Long userId, Booking booking) {
        log.debug("Проверяется, является ли пользователь с ID {} инициатором бронирования", userId);
        return booking.getBooker().getId().equals(userId);
    }

    private void isValid(NewBookingDtoRequest dtoRequest) {
        log.debug("Проверка валидности дат бронирования");
        if (dtoRequest.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата окончания бронирования не может быть в прошлом.");
        }

        if (dtoRequest.getEnd().isEqual(dtoRequest.getStart())) {
            throw new ValidationException("Дата окончания бронирования не может быть равна дате его начала.");
        }

        if (dtoRequest.getStart().isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new ValidationException("Дата начала бронирования не может быть в прошлом.");
        }
    }
}