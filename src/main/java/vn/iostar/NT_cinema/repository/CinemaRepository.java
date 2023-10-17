package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Cinema;

import java.util.Optional;

@Repository
public interface CinemaRepository extends MongoRepository<Cinema, String> {

    Optional<Cinema> findByCinemaName(String cinemaName);
}
