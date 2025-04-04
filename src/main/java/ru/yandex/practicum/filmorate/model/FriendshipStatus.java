package ru.yandex.practicum.filmorate.model;

public enum FriendshipStatus {
    PENDING("Неподтверждённая"),
    CONFIRMED("Подтверждённая");

    private final String description;

    FriendshipStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}