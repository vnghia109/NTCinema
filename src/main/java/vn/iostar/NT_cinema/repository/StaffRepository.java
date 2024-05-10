package vn.iostar.NT_cinema.repository;

import jdk.jfr.Registered;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import vn.iostar.NT_cinema.entity.Cinema;
import vn.iostar.NT_cinema.entity.Role;
import vn.iostar.NT_cinema.entity.Staff;

import java.util.List;

@Registered
public interface StaffRepository extends MongoRepository<Staff, String> {
    Page<Staff> findAllByRoleAndCinema(Role role, Cinema cinema, Pageable pageable);

    List<Staff> findAllByCinema(Cinema cinema);
}
