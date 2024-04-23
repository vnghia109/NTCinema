package vn.iostar.NT_cinema.repository;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
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
    Page<ShowTime> findAllByRoomInOrderByShowTimeIdDesc(List<Room> rooms, Pageable pageable);
    List<ShowTime> findAllByRoomIn(List<Room> rooms);
    Page<ShowTime> findAllByIsDeleteIsFalse(Pageable pageable);
    Page<ShowTime> findAllByRoom_RoomIdOrderByShowTimeIdDesc(String roomId, Pageable pageable);

    Page<ShowTime> findAllByOrderByShowTimeIdDesc(Pageable pageable);

    List<ShowTime> findAllByStatusAndIsSpecialIsFalseAndIsDeleteIsFalse(ShowStatus status);

    List<ShowTime> findAllByIsSpecialIsTrueAndIsDeleteIsFalse();

    List<ShowTime> findAllByRoom_Cinema(Cinema cinema);
}
