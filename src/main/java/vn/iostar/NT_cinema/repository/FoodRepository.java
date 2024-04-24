package vn.iostar.NT_cinema.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.constant.FoodType;
import vn.iostar.NT_cinema.entity.Food;

import java.util.List;

@Repository
public interface FoodRepository extends MongoRepository<Food, String> {
    Page<Food> findAllByFoodTypeAndIsDeleteOrderByFoodIdDesc(FoodType foodType, boolean isDelete, Pageable pageable);

    Page<Food> findAllByIsDeleteOrderByFoodIdDesc(boolean isDelete, Pageable pageable);
}
