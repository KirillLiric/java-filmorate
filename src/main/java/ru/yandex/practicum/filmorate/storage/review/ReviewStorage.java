package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {

    Review createReview(Review review);

    Review updateReview(Review review);

    boolean deleteReviewById(int id);

    Review getReviewById(int id);

    Collection<Review> getReviewsByFilmId(int id, int count);

    Collection<Review> getAllReviews();

    Review addLike(int reviewId, int userId);

    Review removeLike(int reviewId, int userId);

    Review addDislike(int reviewId, int userId);

    Review removeDislike(int reviewId, int userId);
}
