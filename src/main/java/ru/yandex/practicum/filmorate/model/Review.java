package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Review implements Event {
    private int reviewId;
    @NotBlank(message = "Отзыв не может быть пустым")
    private String content;
    @NotNull(message = "Необходимо указать id фильма")
    private Integer filmId;
    private int useful;
    @JsonProperty("isPositive")
    @NotNull(message = "Необходимо указать, является ли отзыв положительным")
    private Boolean isPositive;
    @NotNull(message = "Необходимо указать id пользователя")
    private Long userId;

    @Override
    public long getEntityId() {
        return reviewId;
    }

    @Override
    public long getUserId() {
        return userId;
    }
}
