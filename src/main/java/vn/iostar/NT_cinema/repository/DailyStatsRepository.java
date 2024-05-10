package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Cinema;
import vn.iostar.NT_cinema.entity.DailyStats;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyStatsRepository extends MongoRepository<DailyStats, String> {
    Optional<DailyStats> findByCinemaAndDate(Cinema cinema, LocalDate date);
    List<DailyStats> findByCinemaAndDateBetween(Cinema cinema, LocalDate startDate, LocalDate endDate);
}
