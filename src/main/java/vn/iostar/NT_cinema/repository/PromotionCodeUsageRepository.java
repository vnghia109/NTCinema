package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.PromotionCodeUsage;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionCodeUsageRepository extends MongoRepository<PromotionCodeUsage, String> {
    List<PromotionCodeUsage> findAllByUserIdAndPromotionCodeIdAndDateUsed(String userId, String promotionCodeId, LocalDate dateUsed);
}
