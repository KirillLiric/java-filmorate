package filmorate.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.FilmorateApplication;
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

@SpringBootTest(classes = FilmorateApplication.class)
class FilmorateIntegrationTest {

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private GenreDbStorage genreStorage;

    @Autowired
    private MpaDbStorage mpaStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void shouldCreateUserAddFriendLikeFilmAndGetRecommendations() {
        // Создаем пользователей
        User user1 = createTestUser("user1@mail.com", "user1");
        User user2 = createTestUser("user2@mail.com", "user2");

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        // Добавляем друзей
        userStorage.addFriend(createdUser1.getId(), createdUser2.getId());

        List<User> friends = userStorage.getFriends(createdUser1.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(createdUser2.getId());

        // Создаем фильмы
        Film film1 = createTestFilm("Film 1", 1, List.of(1, 2));
        Film film2 = createTestFilm("Film 2", 2, List.of(3, 4));

        Film createdFilm1 = filmStorage.create(film1);
        Film createdFilm2 = filmStorage.create(film2);

        // Ставим лайки
        filmStorage.addLike(createdFilm1.getId(), createdUser1.getId());
        filmStorage.addLike(createdFilm2.getId(), createdUser1.getId());
        filmStorage.addLike(createdFilm2.getId(), createdUser2.getId());

        // Проверяем популярные фильмы
        List<Film> popularFilms = filmStorage.getPopularFilms(10);
        assertThat(popularFilms)
                .hasSize(2)
                .extracting(Film::getId)
                .containsExactly(createdFilm2.getId(), createdFilm1.getId());
    }

    @Test
    void shouldHandleFilmWithGenresAndMpa() {
        // Получаем данные из справочников
        MpaRating mpa = mpaStorage.getMpaById(1);
        Genre genre1 = genreStorage.getById(1);
        Genre genre2 = genreStorage.getById(2);

        // Создаем фильм
        Film film = Film.builder()
                .name("Test Film")
                .description("Integration Test")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(mpa)
                .genres(List.of(genre1, genre2))
                .build();

        Film createdFilm = filmStorage.create(film);

        // Проверяем сохраненные данные
        Film retrievedFilm = filmStorage.getFilmById(createdFilm.getId());

        assertThat(retrievedFilm.getMpa())
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "G");

        assertThat(retrievedFilm.getGenres())
                .hasSize(2)
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void shouldFindCommonFriends() {
        // Создаем трех пользователей
        User user1 = userStorage.create(createTestUser("user1@mail.com", "user1"));
        User user2 = userStorage.create(createTestUser("user2@mail.com", "user2"));
        User commonFriend = userStorage.create(createTestUser("common@mail.com", "common"));

        // Устанавливаем дружеские связи
        userStorage.addFriend(user1.getId(), commonFriend.getId());
        userStorage.addFriend(user2.getId(), commonFriend.getId());

        // Получаем общих друзей
        List<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(commonFriend.getId());
    }

    private User createTestUser(String email, String login) {
        return User.builder()
                .email(email)
                .login(login)
                .name(login + " Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    private Film createTestFilm(String name, int mpaId, List<Integer> genreIds) {
        return Film.builder()
                .name(name)
                .description("Description for " + name)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new MpaRating(mpaId, null))
                .genres(genreIds.stream()
                        .map(id -> new Genre(id, null))
                        .toList())
                .build();
    }
}
