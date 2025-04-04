package ru.yandex.practicum.filmorate.model;

public enum MpaRating {
    G("G - Нет возрастных ограничений"),
    PG("PG - Детям рекомендуется смотреть с родителями"),
    PG_13("PG-13 - До 13 лет просмотр не желателен"),
    R("R - До 17 лет только с взрослым"),
    NC_17("NC-17 - До 18 лет просмотр запрещён");

    private final String description;

    MpaRating(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}