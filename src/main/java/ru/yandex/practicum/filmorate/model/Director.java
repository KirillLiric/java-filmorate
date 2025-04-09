package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Director {
    private Long id;
    @NotBlank(message = "Имя режиссера не должно быть пустым")
    private String name;
}