package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Cinema;
import vn.iostar.NT_cinema.entity.CinemaFinanceStats;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CinemaFinanceStatsRepository extends MongoRepository<CinemaFinanceStats, String> {
    Optional<CinemaFinanceStats> findByCinemaAndMonth(Cinema cinema, LocalDate month);

    List<CinemaFinanceStats> findAllByCinemaAndMonthBetween(Cinema cinema, LocalDate start, LocalDate end);
}
