package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchUserRequest {
    private String name;
    private String email;

    public boolean hasUserName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasUserEmail() {
        return !(email == null || email.isBlank());
    }
}