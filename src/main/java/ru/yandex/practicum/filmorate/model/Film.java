package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.*;
import ru.yandex.practicum.filmorate.annotations.ValidFilmReleaseDate;

import java.time.LocalDate;
import java.util.*;

@Data
@Builder
public class Film {
    private int id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @ValidFilmReleaseDate
    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительной")
    private int duration;

    private Set<Long> likes = new HashSet<>();
    private MpaRating mpa;
    private List<Genre> genres = new ArrayList<>();
    @NotNull(message = "Режиссер должен быть указан")
    private Long directorsId;
}