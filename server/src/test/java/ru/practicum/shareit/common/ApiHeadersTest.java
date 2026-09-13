package ru.practicum.shareit.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiHeadersTest {

    @Test
    void shouldContainCorrectHeaderName() {

        String expectedHeader = "X-Sharer-User-Id";
        String actualHeader = ApiHeaders.X_SHARER_USER_ID;

        assertEquals(expectedHeader, actualHeader,
                "Заголовок должен называться именно так, иначе фронтенд или шлюз не поймут запрос");
    }
}