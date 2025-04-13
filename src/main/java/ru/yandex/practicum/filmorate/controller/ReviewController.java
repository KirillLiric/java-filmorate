package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    ReviewStorage reviewStorage;

    ReviewController(@Qualifier("ReviewDbStorage") ReviewStorage reviewStorage) {
        this.reviewStorage = reviewStorage;
    }

    @PostMapping
    public Review createReview(@Valid @RequestBody Review review) {
        return reviewStorage.createReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewStorage.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public boolean deleteReviewById(@PathVariable int id) {
        return reviewStorage.deleteReviewById(id);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable int id) {
        return reviewStorage.getReviewById(id);
    }

    @GetMapping
    public Collection<Review> getReviewsByFilmId(@RequestParam(defaultValue = "-1") int filmId,
                                                 @RequestParam(defaultValue = "10") int count) {
        return reviewStorage.getReviewsByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public Review addLike(@PathVariable int id, @PathVariable int userId) {
        return reviewStorage.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public Review addDislike(@PathVariable int id, @PathVariable int userId) {
        return reviewStorage.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Review removeLike(@PathVariable int id, @PathVariable int userId) {
        return reviewStorage.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public Review removeDislike(@PathVariable int id, @PathVariable int userId) {
        return reviewStorage.removeDislike(id, userId);
    }
}
