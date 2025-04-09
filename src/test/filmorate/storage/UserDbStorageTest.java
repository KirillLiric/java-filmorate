package filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ContextConfiguration(classes = {UserDbStorage.class})
class UserDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void shouldCreateAndFindUserById() {
        User user = createTestUser("user@mail.com", "login");
        User createdUser = userStorage.create(user);

        User foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", createdUser.getId())
                .hasFieldOrPropertyWithValue("email", user.getEmail())
                .hasFieldOrPropertyWithValue("login", user.getLogin());
    }

    @Test
    void shouldUpdateUser() {
        User user = createTestUser("old@mail.com", "oldLogin");
        User createdUser = userStorage.create(user);

        User updatedUser = User.builder()
                .id(createdUser.getId())
                .email("new@mail.com")
                .login("newLogin")
                .name("New Name")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        User result = userStorage.update(updatedUser);

        assertThat(result)
                .hasFieldOrPropertyWithValue("email", "new@mail.com")
                .hasFieldOrPropertyWithValue("login", "newLogin")
                .hasFieldOrPropertyWithValue("name", "New Name");
    }

    @Test
    void shouldAddAndGetFriends() {
        User user1 = userStorage.create(createTestUser("user1@mail.com", "login1"));
        User user2 = userStorage.create(createTestUser("user2@mail.com", "login2"));

        userStorage.addFriend(user1.getId(), user2.getId());

        List<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(user2.getId());
    }

    private User createTestUser(String email, String login) {
        return User.builder()
                .email(email)
                .login(login)
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }
}