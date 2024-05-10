package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Cinema;
import vn.iostar.NT_cinema.entity.MonthlyStats;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyStatsRepository extends MongoRepository<MonthlyStats, String> {
    Optional<MonthlyStats> findByCinemaAndMonth(Cinema cinema, LocalDate month);
    List<MonthlyStats> findByCinemaAndMonthBetween(Cinema cinema, LocalDate startDate, LocalDate endDate);
}
