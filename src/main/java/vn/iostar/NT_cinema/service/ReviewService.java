package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.ReviewReq;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.entity.Review;
import vn.iostar.NT_cinema.entity.User;
import vn.iostar.NT_cinema.repository.MovieRepository;
import vn.iostar.NT_cinema.repository.ReviewRepository;
import vn.iostar.NT_cinema.repository.UserRepository;

import java.util.Optional;

@Service
public class ReviewService {
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    MovieRepository movieRepository;

    public ResponseEntity<?> reviewMovie(ReviewReq req, String userId, String movieId) {
            try {
                Optional<Movie> movieOptional = movieRepository.findById(movieId);
                if (movieOptional.isEmpty()){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(GenericResponse.builder()
                                    .success(false)
                                    .message("Movie not found")
                                    .result(null)
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build());
                }
                Review review =new Review();
                review.setComment(req.getComment());
                review.setRating(req.getRating());
                review.setUser(userId);
                review.setMovie(movieId);

                Review reviewRes = reviewRepository.save(review);
                movieOptional.get().addReview(reviewRes);
                movieRepository.save(movieOptional.get());

                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Review movie success")
                                .result(reviewRes)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message(e.getMessage())
                                .result("")
                                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .build());
            }
    }
}
