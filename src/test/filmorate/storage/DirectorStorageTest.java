package filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorRowMapper;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {DirectorDbStorage.class, DirectorRowMapper.class})
public class DirectorStorageTest {
    @Autowired
    private DirectorDbStorage directorDbStorage;

    @Test
    public void testUpdateDirector() {
        Director director = new Director();
        director.setName("Кристофер Нолан");
        directorDbStorage.create(director);
        director.setName("Мартин Скорсезе");

        assertThat(directorDbStorage.update(director))
                .isNotNull()
                .hasFieldOrProperty("id")
                .hasFieldOrPropertyWithValue("name", "Мартин Скорсезе");
    }

    @Test
    public void testCreateDirector() {
        Director director = new Director();
        director.setName("Кристофер Нолан");

        assertThat(directorDbStorage.create(director))
                .isNotNull()
                .hasFieldOrProperty("id")
                .hasFieldOrPropertyWithValue("name", "Кристофер Нолан");
    }

    @Test
    public void testGetDirectorById() {
        Director director1 = new Director();
        Director director2 = new Director();
        Director director3 = new Director();
        director1.setName("Кристофер Нолан");
        director2.setName("Джордж Лукас");
        director3.setName("Квентин Тарантино");
        directorDbStorage.create(director1);
        directorDbStorage.create(director2);
        directorDbStorage.create(director3);
        assertThat(directorDbStorage.getDirectorById(director3.getId()))
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Квентин Тарантино");
    }

    @Test
    public void testGetAllDirectors() {
        Director director1 = new Director();
        Director director2 = new Director();
        Director director3 = new Director();
        director1.setName("Кристофер Нолан");
        director2.setName("Джордж Лукас");
        director3.setName("Квентин Тарантино");
        directorDbStorage.create(director1);
        directorDbStorage.create(director2);
        directorDbStorage.create(director3);
        assertThat(directorDbStorage.getAllDirectors())
                .isNotEmpty()
                .hasSize(3);
    }

    @Test
    public void testDeleteDirectors() {
        Director director1 = new Director();
        Director director2 = new Director();
        Director director3 = new Director();
        director1.setName("Кристофер Нолан");
        director2.setName("Джордж Лукас");
        director3.setName("Квентин Тарантино");
        directorDbStorage.create(director1);
        directorDbStorage.create(director2);
        directorDbStorage.create(director3);
        assertThat(directorDbStorage.delete(director1.getId())).isTrue();
    }
}
