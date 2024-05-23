package vn.iostar.NT_cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.PromotionCode;

@Repository
public interface PromotionCodeRepository extends MongoRepository<PromotionCode, String> {
    boolean existsByPromotionCode(String promotionCode);

    Page<PromotionCode> findAllByPromotionCode(String promotionCode, Pageable pageable);
}
