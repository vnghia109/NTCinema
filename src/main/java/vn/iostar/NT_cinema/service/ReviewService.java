package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.ReviewReq;
import vn.iostar.NT_cinema.dto.ReviewRes;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.entity.Review;
import vn.iostar.NT_cinema.entity.User;
import vn.iostar.NT_cinema.repository.MovieRepository;
import vn.iostar.NT_cinema.repository.ReviewRepository;
import vn.iostar.NT_cinema.repository.UserRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
                review.setUserName(userRepository.findById(userId).get().getUserName());
                review.setMovieId(movieId);
                review.setMovieName(movieRepository.findById(movieId).get().getTitle());
                review.setCreateAt(new Date());

                Review reviewRes = reviewRepository.save(review);
                movieOptional.get().addReview(reviewRes);
                movieRepository.save(movieOptional.get());

                ReviewRes reviewRes1 = new ReviewRes(reviewRes.getMovieName(), reviewRes.getUserName(), reviewRes.getComment(), reviewRes.getRating());
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Review movie success")
                                .result(reviewRes1)
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

    public ResponseEntity<GenericResponse> getReviews(Pageable pageable) {
        try {
            Page<Review> reviews = reviewRepository.findAllByOrderByCreateAtDesc(pageable);

            Map<String, Object> map = new HashMap<>();
            map.put("content", reviews.getContent());
            map.put("pageNumber", reviews.getPageable().getPageNumber() + 1);
            map.put("pageSize", reviews.getSize());
            map.put("totalPages", reviews.getTotalPages());
            map.put("totalElements", reviews.getTotalElements());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Get all review success")
                            .result(map)
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
