package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Staff;
import vn.iostar.NT_cinema.entity.StaffStats;

import java.util.List;

@Repository
public interface StaffStatsRepository extends MongoRepository<StaffStats, String> {
    List<StaffStats> findAllByOrderByTotalOfTicketsDesc();

    List<StaffStats> findAllByStaffIn(List<Staff> staffs);
}
