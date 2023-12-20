package vn.iostar.NT_cinema.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Cinema;
import vn.iostar.NT_cinema.entity.Manager;
import vn.iostar.NT_cinema.entity.Role;

import java.util.Optional;

@Repository
public interface ManagerRepository extends MongoRepository<Manager, String> {
    Optional<Manager> findByCinema(Cinema cinema);

    Page<Manager> findAllByRole(Role Role, Pageable pageable);
}
