package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Seat;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends MongoRepository<Seat, String> {
    Optional<Seat> findByColumnAndRowAndShowTimeIdAndTimeShowAndStatusIsTrue(String column, String row, String showTimeId, Date timeShow);
    List<Seat> findAllByShowTimeIdAndTimeShowAndStatusIsFalse(String showtimeId, Date timeShow);
    List<Seat> findAllByTimeShowAndStatusIsFalse(Date timeShow);
}
