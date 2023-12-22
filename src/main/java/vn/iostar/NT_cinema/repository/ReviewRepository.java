package vn.iostar.NT_cinema.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.history.support.RevisionEntityInformation;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Review;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    Page<Review> findAllByOrderByCreateAtDesc(@NotNull Pageable pageable);
}
