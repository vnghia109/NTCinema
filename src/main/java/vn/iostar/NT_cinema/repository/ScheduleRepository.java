package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Schedule;

import java.util.List;

@Repository
public interface ScheduleRepository extends MongoRepository<Schedule, String> {
    List<Schedule> findAllByRoomId(String roomId);

    List<Schedule> findAllByShowTimeId(String showTimeId);
}
