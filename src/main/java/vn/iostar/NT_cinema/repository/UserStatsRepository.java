package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.UserStats;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatsRepository extends MongoRepository<UserStats, String> {
    Optional<UserStats> findByUser_UserId(String userId);
    List<UserStats> findAllByOrderByTotalOfTicketsDesc();
}
