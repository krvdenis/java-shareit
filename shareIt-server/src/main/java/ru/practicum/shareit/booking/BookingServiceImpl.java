package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BookingConflictException;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
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
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto createBookingRequest(long userId, NewBookingRequestDto dtoRequest) {
        log.debug("Попытка оформить новое бронирование: {}", dtoRequest);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        Item item = itemRepository.findById(dtoRequest.getItemId())
                .orElseThrow(() -> new NotFoundException("Item с ID %d не найден".formatted(dtoRequest.getItemId())));
        isValid(dtoRequest);

        if (!item.getAvailable()) {
            throw new BookingConflictException("Данная вещь недоступна для бронирования");
        }

        long conflict = repository.countConflicts(dtoRequest.getItemId(), dtoRequest.getStart(), dtoRequest.getEnd());
        if (conflict > 0) {
            throw new BookingConflictException("Невозможно забронировать: на это время уже есть активная бронь");
        }

        Booking booking = BookingMapper.mapToBooking(dtoRequest, user, item);
        BookingDto bookingDto = BookingMapper.mapToBookingDto(repository.save(booking));
        log.info("Бронирование успешно зарегистрировано под ID {}", bookingDto.getId());
        return bookingDto;
    }

    @Override
    public BookingDto getBookingById(long userId, long bookingId) {
        log.debug("Попытка получить информацию о бронировании с ID: {}", bookingId);
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID %d не найден".formatted(bookingId)));

        if (!isOwner(userId, booking) && !isBooker(userId, booking)) {
            throw new NoAccessException("Только владелец вещи или автор бронирования может получить данные о" +
                    " конкретном бронировании");
        }
        BookingDto bookingDto = BookingMapper.mapToBookingDto(booking);
        log.info("Отправлена информация бронировании вещи c ID: {}", bookingDto.getId());
        return bookingDto;
    }

    @Override
    public List<BookingDto> getBookingsByBookerId(long userId, State state, Integer from, Integer size) {
        log.debug("Попытка получить список всех бронирований пользователем с ID:{}", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователя с ID " + userId + " не существует");
        }

        Sort sort = Sort.by("startTime").descending();
        Pageable pageable = PageRequest.of(from, size, sort);

        Page<Booking> bookingPage;

        switch (state) {
            case ALL -> bookingPage = repository.findByBookerIdOrderByStartTimeDesc(userId, pageable);
            case CURRENT -> bookingPage = repository.findCurrentBookingsByBookerId(userId, pageable);
            case PAST -> bookingPage = repository.findPastByBookerId(userId, pageable);
            case FUTURE -> bookingPage = repository.findFutureByBookerId(userId, pageable);
            case WAITING -> bookingPage = repository.findByBookerIdAndStatusOrderByStartTimeDesc(userId, WAITING,
                    pageable);
            case REJECTED -> bookingPage = repository.findByBookerIdAndStatusOrderByStartTimeDesc(userId, REJECTED,
                    pageable);
            default -> throw new ValidationException("Обработан неизвестный статус: " + state);
        }

        List<BookingDto> bookingDtos = bookingPage.getContent().stream()
                .map(BookingMapper::mapToBookingDto)
                .collect(Collectors.toList());
        log.info("Отправлен список из {} сущностей бронирования", bookingDtos.size());
        return bookingDtos;
    }

    @Override
    public List<BookingDto> getBookingsByOwnerId(long ownerId, State state) {
        log.debug("Попытка получить список всех бронирований у пользователя с ID:{}", ownerId);
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("Пользователя с ID " + ownerId + " не существует");
        }

        List<Booking> bookings;
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
    public BookingDto updateBookingStatus(long userId, long bookingId, Boolean status) {
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
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователя с ID " + userId + " не существует");
        }

        if (status == true) {
            booking.setStatus(BookingStatus.APPROVED);
            log.debug("Изменение статуса на {}", BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
            log.debug("Изменение статуса на {}", BookingStatus.REJECTED);
        }
        BookingDto bookingDto = BookingMapper.mapToBookingDto(repository.save(booking));
        log.info("Изменение статуса бронирования успешно завершено");
        return bookingDto;
    }

    private boolean isOwner(long userId, Booking booking) {
        log.debug("Проверяется, является ли пользователь с ID {} собственником", userId);
        Long ownerId = booking.getItem().getOwner().getId();
        return ownerId.equals(userId);
    }

    private boolean isBooker(long userId, Booking booking) {
        log.debug("Проверяется, является ли пользователь с ID {} инициатором бронирования", userId);
        Long bookerId = booking.getBooker().getId();
        return bookerId.equals(userId);
    }

    private void isValid(NewBookingRequestDto dtoRequest) {
        log.debug("Проверка валидности дат бронирования");
        if (dtoRequest.getEnd().isEqual(dtoRequest.getStart())) {
            throw new ValidationException("Дата и время завершения бронирования и начала не должны совпадать.");
        }
        if (dtoRequest.getEnd().isBefore(dtoRequest.getStart())) {
            throw new ValidationException("Дата и время завершения бронирования не может быть раньше начала " +
                    "бронирования.");
        }
    }
}