package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    @Autowired
    NotificationService notificationService;

    public ResponseEntity<?> reviewMovie(ReviewReq req, String userId, String movieId) {
            try {
                Optional<Movie> movieOptional = movieRepository.findById(movieId);
                if (movieOptional.isEmpty()){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(GenericResponse.builder()
                                    .success(false)
                                    .message("Phim không tìm thấy.")
                                    .result(null)
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build());
                }
                Review review =new Review();
                review.setComment(req.getComment());
                review.setRating(req.getRating());
                Optional<User> user = userRepository.findById(userId);
                if (user.isEmpty()){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(GenericResponse.builder()
                                    .success(false)
                                    .message("Không tìm thấy người dùng.")
                                    .result(null)
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build());
                }
                review.setUserName(user.get().getUserName());
                review.setMovieId(movieId);
                review.setMovieName(movieOptional.get().getTitle());
                review.setCreateAt(new Date());

                Review reviewRes = reviewRepository.save(review);
                movieOptional.get().addReview(reviewRes);
                movieRepository.save(movieOptional.get());
                notificationService.reviewNotification(reviewRes, user.get());

                ReviewRes reviewRes1 = new ReviewRes(reviewRes.getMovieName(), reviewRes.getUserName(), reviewRes.getComment(), reviewRes.getRating());
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Đánh giá phim thành công!")
                                .result(reviewRes1)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
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
                            .message("Lấy tất cả đánh giá thành công!")
                            .result(map)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
