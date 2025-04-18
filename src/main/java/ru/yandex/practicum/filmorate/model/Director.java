package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id"})
public class Director {
    private Long id;
    @NotBlank(message = "Имя режиссера не должно быть пустым")
    private String name;
}