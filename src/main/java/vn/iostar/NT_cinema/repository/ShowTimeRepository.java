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
    Optional<ShowTime> findByMovieAndStatusIsTrueAndRoom_RoomId (Movie movie, String roomId);
    List<ShowTime> findByMovieAndStatusIsTrue (Movie movie);
    List<ShowTime> findAllByMovieAndIsSpecialIsFalseAndStatusIsTrue (Movie movie);
    Optional<ShowTime> findByMovie_MovieIdAndIsSpecialIsTrueAndStatusIsTrue (String movieId);
    List<ShowTime> findAllByRoom_Cinema_CinemaIdAndStatusIsTrue(String cinemaId);
    Page<ShowTime> findAllByRoom_Cinema(Cinema cinema, Pageable pageable);
    Page<ShowTime> findAllByRoomIn(List<Room> rooms, Pageable pageable);
    List<ShowTime> findAllByRoom(Room room);
    Page<ShowTime> findAllByStatusIsTrue(Pageable pageable);
    List<ShowTime> findAllByRoom_RoomIdAndStatusIsTrue(String roomId);
    @Override
    @NotNull
    Page<ShowTime> findAll(@NotNull Pageable pageable);
}
