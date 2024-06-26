package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Schedule;
import vn.iostar.NT_cinema.entity.Seat;
import vn.iostar.NT_cinema.entity.ShowTime;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends MongoRepository<Seat, String> {
    Optional<Seat> findByColumnAndRowAndShowTimeAndScheduleAndStatusIsTrue(String column, String row, ShowTime showTime, Schedule schedule);
    List<Seat> findAllByShowTimeAndScheduleAndStatusIsFalse(ShowTime showtime, Schedule schedule);

    List<Seat> findAllByShowTimeIn(List<ShowTime> showTimes);

    List<Seat> findAllByStatusIsFalse();
}
