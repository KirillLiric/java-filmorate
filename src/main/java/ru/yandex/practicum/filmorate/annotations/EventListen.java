package ru.yandex.practicum.filmorate.annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventListen {
    String eventType();
    String operation();
    int userIdArgIndex() default 0;
    int entityIdArgIndex() default 1;
    boolean argIsEvent() default false;
}
