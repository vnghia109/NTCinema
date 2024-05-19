package vn.iostar.NT_cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.constant.ShowStatus;
import vn.iostar.NT_cinema.entity.Cinema;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.entity.Room;
import vn.iostar.NT_cinema.entity.ShowTime;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowTimeRepository extends MongoRepository<ShowTime, String> {
    Optional<ShowTime> findByMovieAndRoomAndIsDeleteIsFalseAndIsSpecialIsFalse (Movie movie, Room room);
    Optional<ShowTime> findByMovieAndRoomAndIsDeleteIsFalseAndIsSpecialIsTrue (Movie movie, Room room);
    List<ShowTime> findByMovieAndIsDeleteIsFalse (Movie movie);
    Page<ShowTime> findAllByRoomInAndStatusAndIsDeleteIsFalseAndIsSpecialIsFalse(List<Room> rooms, ShowStatus status, Pageable pageable);

    List<ShowTime> findAllByRoomIn(List<Room> rooms);

    List<ShowTime> findAllByStatusAndIsSpecialIsFalseAndIsDeleteIsFalse(ShowStatus status);

    List<ShowTime> findAllByStatusAndRoomInAndIsSpecialIsFalseAndIsDeleteIsFalse(ShowStatus status, List<Room> rooms);

    List<ShowTime> findAllByIsSpecialIsTrueAndIsDeleteIsFalse();

    List<ShowTime> findAllByRoomInAndIsSpecialIsTrueAndIsDeleteIsFalse(List<Room> rooms);

    List<ShowTime> findAllByRoom_Cinema(Cinema cinema);

    List<ShowTime> findByTimeStartBetween(Date timeStart, Date timeStart2);
}
