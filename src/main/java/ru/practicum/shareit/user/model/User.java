package ru.practicum.shareit.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Entity
@Table(name = "users")
@Getter @Setter @ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Item> items = new ArrayList<>();
    @OneToMany(mappedBy = "booker", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();
    @OneToMany(mappedBy = "requester", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ItemRequest> requests = new ArrayList<>();
    @OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
}