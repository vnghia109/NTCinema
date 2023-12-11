package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.constant.PriceType;
import vn.iostar.NT_cinema.entity.Price;

import java.util.Optional;

@Repository
public interface PriceRepository extends MongoRepository<Price, String> {
    Optional<Price> findByType(PriceType type);
}
