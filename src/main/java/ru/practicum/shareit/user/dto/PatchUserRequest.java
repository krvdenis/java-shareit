package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class PatchUserRequest {
    private String name;
    @Email
    private String email;

    public boolean hasUserName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasUserEmail() {
        return !(email == null || email.isBlank());
    }
}