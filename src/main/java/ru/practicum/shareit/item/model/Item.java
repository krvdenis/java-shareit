package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
//@Entity
//@Table(name = "items")
//@Getter @Setter @ToString
//public class Item {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    private String name;
//    private String description;
//    @Column(name = "is_available")
//    private Boolean available;
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "owner_id")
//    private User owner;
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "request_id")
//    private ItemRequest request;
//    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE, orphanRemoval = true)
//    private List<Booking> bookings = new ArrayList<>();
//    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE, orphanRemoval = true)
//    private List<Comment> comments = new ArrayList<>();
//}

@Entity
@Table(name = "items")
@Getter @Setter @ToString
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @Column(name = "is_available")
    private Boolean available;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private ItemRequest request;
    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();
    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
}