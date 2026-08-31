package ru.practicum.shareit.booking;

public enum State {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static State from(String state) {
        return switch (state.toLowerCase()) {
            case "all" -> ALL;
            case "current" -> CURRENT;
            case "past" -> PAST;
            case "future" -> FUTURE;
            case "waiting" -> WAITING;
            case "rejected" -> REJECTED;
            default -> null;
        };
    }
}