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
    Optional<ShowTime> findByMovieAndRoomAndIsDeleteIsFalse (Movie movie, Room room);
    List<ShowTime> findByMovieAndIsDeleteIsFalse (Movie movie);
    List<ShowTime> findAllByRoomInAndStatusAndIsDeleteIsFalseAndIsSpecialIsFalse(List<Room> rooms, ShowStatus status);
    Page<ShowTime> findAllByRoomIn(List<Room> rooms, Pageable pageable);
    List<ShowTime> findAllByRoomIn(List<Room> rooms);
    Page<ShowTime> findAllByIsDeleteIsFalse(Pageable pageable);
    List<ShowTime> findAllByRoom_RoomId(String roomId);
    @Override
    @NotNull
    Page<ShowTime> findAll(@NotNull Pageable pageable);

    List<ShowTime> findAllByStatusAndIsSpecialIsFalseAndIsDeleteIsFalse(ShowStatus status);

    List<ShowTime> findAllByIsSpecialIsTrueAndIsDeleteIsFalse();
}
