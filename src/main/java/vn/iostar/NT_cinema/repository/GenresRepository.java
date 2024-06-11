package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Genres;

import java.util.Optional;

@Repository
public interface GenresRepository extends MongoRepository<Genres, String> {
    Optional<Genres> findByName(String name);
}
