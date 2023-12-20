package vn.iostar.NT_cinema.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Cinema;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.entity.Room;
import vn.iostar.NT_cinema.entity.ShowTime;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShowTimeRepository extends MongoRepository<ShowTime, String> {
    Optional<ShowTime> findByMovieAndStatusIsTrueAndRoom (Movie movie, Room room);
    List<ShowTime> findByMovieAndStatusIsTrue (Movie movie);
    List<ShowTime> findAllByMovieAndIsSpecialIsFalseAndStatusIsTrue (Movie movie);
    Optional<ShowTime> findByMovieAndIsSpecialIsTrueAndStatusIsTrue (Movie movie);
    List<ShowTime> findAllByRoomInAndStatusIsTrue(List<Room> rooms);
    Page<ShowTime> findAllByRoomIn(List<Room> rooms, Pageable pageable);
    Page<ShowTime> findAllByStatusIsTrue(Pageable pageable);
    List<ShowTime> findAllByRoom_RoomIdAndStatusIsTrue(String roomId);
    @Override
    @NotNull
    Page<ShowTime> findAll(@NotNull Pageable pageable);
}
