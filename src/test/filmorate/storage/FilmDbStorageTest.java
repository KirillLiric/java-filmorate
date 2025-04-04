package filmorate.storage;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ContextConfiguration(classes = {FilmDbStorage.class, UserDbStorage.class, MpaDbStorage.class, GenreDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private MpaDbStorage mpaStorage;

    @Autowired
    private GenreDbStorage genreStorage;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void shouldCreateAndFindFilmById() {
        Film film = createTestFilm();
        Film createdFilm = filmStorage.create(film);

        Film foundFilm = filmStorage.getFilmById(createdFilm.getId());

        assertThat(foundFilm)
                .hasFieldOrPropertyWithValue("id", createdFilm.getId())
                .hasFieldOrPropertyWithValue("name", film.getName())
                .hasFieldOrPropertyWithValue("description", film.getDescription());

        assertThat(foundFilm.getGenres())
                .hasSize(2)
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void shouldUpdateFilm() {
        Film film = createTestFilm();
        Film createdFilm = filmStorage.create(film);

        Film updatedFilm = Film.builder()
                .id(createdFilm.getId())
                .name("Updated Name")
                .description("Updated Description")
                .releaseDate(LocalDate.of(2005, 5, 5))
                .duration(150)
                .mpa(new MpaRating(2, null))
                .genres(List.of(new Genre(3, null)))
                .build();

        Film result = filmStorage.update(updatedFilm);

        assertThat(result)
                .hasFieldOrPropertyWithValue("name", "Updated Name")
                .hasFieldOrPropertyWithValue("duration", 150);

        assertThat(result.getGenres())
                .hasSize(1)
                .extracting(Genre::getId)
                .containsExactly(3);
    }

    @Test
    void shouldAddAndRemoveLike() {
        // Создаем тестового пользователя
        User testUser = User.builder()
                .email("user@mail.com")
                .login("testLogin")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User createdUser = userStorage.create(testUser);

        // Создаем тестовый фильм
        Film testFilm = createTestFilm();
        Film createdFilm = filmStorage.create(testFilm);

        // Добавляем лайк
        filmStorage.addLike(createdFilm.getId(), createdUser.getId());
        Film filmWithLike = filmStorage.getFilmById(createdFilm.getId());
        assertThat(filmWithLike.getLikes()).contains(createdUser.getId());

        // Удаляем лайк
        filmStorage.removeLike(createdFilm.getId(), createdUser.getId());
        Film filmWithoutLike = filmStorage.getFilmById(createdFilm.getId());
        assertThat(filmWithoutLike.getLikes()).doesNotContain(createdUser.getId());
    }

    private Film createTestFilm() {
        return Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new MpaRating(1, null))
                .genres(List.of(
                        new Genre(1, null),
                        new Genre(2, null)
                ))
                .build();
    }
}