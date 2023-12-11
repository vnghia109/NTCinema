package vn.iostar.NT_cinema.repository;

import org.jetbrains.annotations.NotNull;
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
    Optional<ShowTime> findByMovieAndStatusIsTrueAndRoom_RoomId (Movie movie, String roomId);
    List<ShowTime> findByMovieAndStatusIsTrue (Movie movie);
    List<ShowTime> findAllByMovieAndIsSpecialIsFalseAndStatusIsTrue (Movie movie);
    Optional<ShowTime> findByMovie_MovieIdAndIsSpecialIsTrueAndStatusIsTrue (String movieId);
    List<ShowTime> findAllByRoom_Cinema_CinemaIdAndStatusIsTrue(String cinemaId);
    Page<ShowTime> findAllByRoom_Cinema_CinemaId(String cinemaId, Pageable pageable);
    Page<ShowTime> findAllByStatusIsTrue(Pageable pageable);
    @Override
    @NotNull
    Page<ShowTime> findAll(@NotNull Pageable pageable);
}
