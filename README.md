# java-filmorate
Template repository for Filmorate project.

![Filmorate_schema](./src/main/resources/Filmorate.png)

 Основные сущности:

1) users - информация о пользователях

2) films - данные о фильмах

3) mpa_ratings - справочник возрастных рейтингов

4) genres - справочник жанров

Получение топ-10 популярных фильмов: 

SELECT f.id, f.name, COUNT(l.user_id) AS likes_count
FROM films f
LEFT JOIN likes l ON f.id = l.film_id
GROUP BY f.id
ORDER BY likes_count DESC
LIMIT 10;

Получение фильмов по жанру:

SELECT f.*
FROM films f
JOIN film_genres fg ON f.id = fg.film_id
JOIN genres g ON fg.genre_id = g.id
WHERE g.name = 'Комедия';

Получение общих друзей SELECT u.*
FROM users u
JOIN friendships f1 ON u.id = f1.friend_id AND f1.user_id = 123
JOIN friendships f2 ON u.id = f2.friend_id AND f2.user_id = 456
WHERE f1.status = 'CONFIRMED' AND f2.status = 'CONFIRMED';

Добавление нового фильма

INSERT INTO films (name, description, release_date, duration, mpa_rating)
VALUES ('Новый фильм', 'Описание нового фильма', '2023-01-01', 120, 'PG-13')
RETURNING *;