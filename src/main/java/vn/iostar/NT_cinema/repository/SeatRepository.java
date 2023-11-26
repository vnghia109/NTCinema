package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Seat;

import java.util.Optional;

@Repository
public interface SeatRepository extends MongoRepository<Seat, String> {
    Optional<Seat> findByColumnAndRowAndShowTimeIdAndTimeShowAndStatusIsTrue(String column, String row, String showTimeId, String timeShow);
}
