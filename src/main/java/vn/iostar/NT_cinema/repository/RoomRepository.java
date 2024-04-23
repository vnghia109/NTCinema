package vn.iostar.NT_cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Cinema;
import vn.iostar.NT_cinema.entity.Room;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends MongoRepository<Room, String> {
    Optional<Room> findByRoomNameAndCinema(String roomName, Cinema cinema);

    Page<Room> findAllByCinemaOrderByRoomIdDesc(Cinema cinema, Pageable pageable);
    List<Room> findAllByCinema(Cinema cinema);
    List<Room> findAllByCinema_CinemaId(String cinemaId);
    Page<Room> findAllByCinema_CinemaIdOrderByRoomIdDesc(String cinemaId, Pageable pageable);

    Page<Room> findAllByOrderByRoomIdDesc(Pageable pageable);
}
