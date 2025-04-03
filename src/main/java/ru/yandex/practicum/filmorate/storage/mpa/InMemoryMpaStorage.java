package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.RatingNotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryMpaStorage implements MpaStorage {
    private final Map<Integer, MpaRating> ratings = new HashMap<>();
    private int idCounter = 0;

    public InMemoryMpaStorage() {
        // Инициализация тестовыми данными
        save(new MpaRating(1, "G"));
        save(new MpaRating(2, "PG"));
        save(new MpaRating(3, "PG-13"));
        save(new MpaRating(4, "R"));
        save(new MpaRating(5, "NC-17"));
    }

    private void save(MpaRating rating) {
        rating.setId(++idCounter);
        ratings.put(rating.getId(), rating);
    }

    @Override
    public Collection<MpaRating> getAllRatings() {
        return ratings.values();
    }

    @Override
    public MpaRating getMpaById(Integer id) {
        if (!ratings.containsKey(id)) {
            throw new RatingNotFoundException("Рейтинг MPA с id " + id + " не найден");
        }
        return ratings.get(id);
    }

    @Override
    public boolean delete(Integer id) {
        if (!ratings.containsKey(id)) {
            throw new RatingNotFoundException("Рейтинг MPA с id " + id + " не найден");
        }
        ratings.remove(id);
        return true;
    }
}