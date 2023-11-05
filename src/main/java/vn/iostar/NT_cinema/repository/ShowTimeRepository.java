package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.entity.ShowTime;

import java.util.List;

@Repository
public interface ShowTimeRepository extends MongoRepository<ShowTime, String> {
    List<ShowTime> findAllByMovieOrderByTimeAsc (Movie movie);
    List<ShowTime> findAllByMovieAndIsSpecialIsTrue (Movie movie);
    List<ShowTime> findAllByRoom_Cinema_CinemaId(String cinemaId);
}
