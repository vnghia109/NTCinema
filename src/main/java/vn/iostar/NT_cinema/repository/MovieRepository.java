package vn.iostar.NT_cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Movie;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {
    Page<Movie> findAllByIsDeleteIsFalseOrderByMovieIdDesc(Pageable pageable);

    Page<Movie> findAllByOrderByMovieIdDesc(Pageable pageable);

    Optional<Movie> findByTitle(String name);


    List<Movie> findByOrderByRatingDesc();
}
