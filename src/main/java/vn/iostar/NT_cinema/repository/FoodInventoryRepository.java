package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Cinema;
import vn.iostar.NT_cinema.entity.Food;
import vn.iostar.NT_cinema.entity.FoodInventory;

import java.util.Optional;

@Repository
public interface FoodInventoryRepository extends MongoRepository<FoodInventory, String> {
    Optional<FoodInventory> findByFoodAndCinema(Food food, Cinema cinema);
}
