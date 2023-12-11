package vn.iostar.NT_cinema.repository;

import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Movie;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {
    Page<Movie> findAllByIsDeleteIsFalse(Pageable pageable);

    @Override
    @NotNull
    Page<Movie> findAll(@NotNull Pageable pageable);

    Optional<Movie> findByTitle(String name);

    @Query("{$or: [{ 'title': { $regex: ?0, $options: 'i' } }, { 'actor': { $regex: ?0, $options: 'i' } }, { 'genres': { $regex: ?0, $options: 'i' } }]}")
    List<Movie> searchMoviesByKeyword(String keyword);
}
