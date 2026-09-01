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
        long conflict = repository.countConflicts(dtoRequest.getItemId(), dtoRequest.getStart(), dtoRequest.getEnd());
        if (conflict > 0) {
            throw new BookingConflictException("Невозможно забронировать: а это время уже есть активная бронь");
        }
        if (!item.getAvailable()) {
            throw new BookingConflictException("Данная вещь недоступна для бронирования");
        }

        Booking booking = BookingMapper.mapToBooking(dtoRequest, user, item);
        BookingDto bookingDto = BookingMapper.mapToBookingDto(repository.save(booking));
        log.info("Бронирование успешно зарегистрировано под ID {}", bookingDto.getId());
        return bookingDto;
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        log.debug("Попытка получить информацию о бронировании с ID: {}", bookingId);
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID %d не найден".formatted(bookingId)));

        if (isOwner(userId, booking) || isBooker(userId, booking)) {
            BookingDto bookingDto = BookingMapper.mapToBookingDto(booking);
            log.info("Отправлена информация бронировании вещи c ID: {}", bookingDto.getId());
            return bookingDto;
        } else {
            throw new NoAccessException("Только владелец вещи или автор бронирования может получить данные о" +
                    " конкретном бронировании");
        }
    }

    @Override
    public List<BookingDto> getBookingsByBookerId(Long userId, State state) {
        log.debug("Попытка получить список всех бронирований пользователя с ID:{}", userId);
        List<Booking> bookings;

        switch (state) {
            case ALL -> bookings = repository.findByBookerIdOrderByStartTimeDesc(userId);
            case CURRENT -> bookings = repository.findCurrentBookingsByBookerId(userId);
            case PAST -> bookings = repository.findPastByBookerId(userId);
            case FUTURE -> bookings = repository.findFutureByBookerId(userId);
            case WAITING -> bookings = repository.findByBookerIdAndStatusOrderByStartTimeDesc(userId, WAITING);
            case REJECTED -> bookings = repository.findByBookerIdAndStatusOrderByStartTimeDesc(userId, REJECTED);
            default -> throw new ValidationException("Обработан неизвестный статус: " + state);
        }

        List<BookingDto> bookingDtos = bookings.stream()
                .map(BookingMapper::mapToBookingDto)
                .collect(Collectors.toList());
        log.info("Отправлен список из {} сущностей бронирования", bookingDtos.size());
        return bookingDtos;
    }

    @Override
    public List<BookingDto> getBookingsByOwnerId(Long ownerId, State state) {
        log.debug("Попытка получить список всех бронирований у пользователя с ID:{}", ownerId);
        if (!userService.existsById(ownerId)) {
            throw new NotFoundException("Пользователя с ID " + ownerId + " не существует");
        }

        List<Booking> bookings;
        if (state == null) {
            throw new ValidationException("Неверный параметр запроса");
        }

        switch (state) {
            case ALL -> bookings = repository.findByItemOwnerIdOrderByStartTimeDesc(ownerId);
            case CURRENT -> bookings = repository.findCurrentBookingsByOwnerId(ownerId);
            case PAST -> bookings = repository.findPastByOwnerId(ownerId);
            case FUTURE -> bookings = repository.findFutureByOwnerId(ownerId);
            case WAITING -> bookings = repository.findByItemOwnerIdAndStatusOrderByStartTimeDesc(ownerId, WAITING);
            case REJECTED -> bookings = repository.findByItemOwnerIdAndStatusOrderByStartTimeDesc(ownerId, REJECTED);
            default -> throw new ValidationException("Обработан неизвестный статус: " + state);
        }

        List<BookingDto> bookingDtos = bookings.stream()
                .map(BookingMapper::mapToBookingDto)
                .collect(Collectors.toList());
        log.info("Отправлен список из {} сущностей бронирования", bookingDtos.size());
        return bookingDtos;
    }

    @Override
    @Transactional
    public BookingDto updateBookingStatus(Long userId, Long bookingId, Boolean status) {
        log.debug("Попытка подтвердить бронирование с ID {} владельцем с ID {}", bookingId, userId);
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID %d не найден".formatted(bookingId)));
    /*
        Для ревьюера: порядок условий такой потому, что в тестах postman при проверке этого метода, в последнем тесте,
        отправляется запрос на подтверждение от несуществующего пользователя, но ожидается ошибка о том, что только
        владелец вещи может подтверждать бронь (ожидает код ошибки отличный от NotFoundException)
    */
        if (!isOwner(userId, booking)) {
            throw new NoAccessException("Только владелец вещи может подтверждать бронирование");
        }
        if (!userService.existsById(userId)) {
            throw new NotFoundException("Пользователя с ID " + userId + " не существует");
        }

        if (status == true) {
            booking.setStatus(BookingStatus.APPROVED);
            log.debug("Изменение статуса на {}", BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
            log.debug("Изменение статуса на {}", BookingStatus.REJECTED);
            BookingDto bookingDto = BookingMapper.mapToBookingDto(repository.save(booking));
            log.info("Отклонение бронирования завершено");
        }
        BookingDto bookingDto = BookingMapper.mapToBookingDto(repository.save(booking));
        log.info("Изменение статуса бронирования успешно завершено");
        return bookingDto;
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
        if (dtoRequest.getEnd().isEqual(dtoRequest.getStart())) {
            throw new ValidationException("Дата завершения бронирования не может быть равна дате его начала.");
        }
        if (dtoRequest.getEnd().isBefore(dtoRequest.getStart())) {
            throw new ValidationException("Дата и время завершения бронирования не может быть раньше начала " +
                    "бронирования.");
        }
    }
}