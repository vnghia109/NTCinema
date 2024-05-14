package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.PromotionFixed;

@Repository
public interface PromotionFixedRepository extends MongoRepository<PromotionFixed, String> {
}
