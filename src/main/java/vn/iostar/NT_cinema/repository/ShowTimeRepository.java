package vn.iostar.NT_cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.entity.ShowTime;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShowTimeRepository extends MongoRepository<ShowTime, String> {
    Optional<ShowTime> findByMovieAndStatusIsTrueAndRoom_Cinema_CinemaId (Movie movie, String cinemaId);
    List<ShowTime> findByMovieAndStatusIsTrue (Movie movie);
    List<ShowTime> findAllByMovieAndIsSpecialIsFalseAndStatusIsTrue (Movie movie);
    Optional<ShowTime> findByMovie_MovieIdAndIsSpecialIsTrueAndStatusIsTrue (String movieId);
    List<ShowTime> findAllByRoom_Cinema_CinemaIdAndStatusIsTrue(String cinemaId);
    Page<ShowTime> findAllByStatusIsTrue(Pageable pageable);
}
