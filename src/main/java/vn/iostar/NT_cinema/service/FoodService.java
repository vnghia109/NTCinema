package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.FoodType;
import vn.iostar.NT_cinema.dto.FoodReq;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.entity.Food;
import vn.iostar.NT_cinema.repository.FoodRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FoodService {
    @Autowired
    FoodRepository foodRepository;

    public ResponseEntity<GenericResponse> addFood(FoodReq foodReq) {
        try {
            Food food = new Food();
            food.setName(foodReq.getName());
            food.setPrice(foodReq.getPrice());
            food.setFoodType(FoodType.valueOf(foodReq.getFoodType()));

            Food foodRes = foodRepository.save(food);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Add food success")
                            .result(foodRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Internal Server Error")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> deleteFood(String id) {
        try {
            Optional<Food> food = foodRepository.findById(id);
            if (food.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Food notfound")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());

            foodRepository.delete(food.get());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Delete food success")
                            .result(null)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Internal Server Error")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> updateIsDeleteFood(String id) {
        try {
            Optional<Food> food = foodRepository.findById(id);
            if (food.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Food notfound")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());

            food.get().setStatus(!food.get().isStatus());

            Food foodRes = foodRepository.save(food.get());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Delete food success")
                            .result(foodRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Internal Server Error")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> updateFood(String id, FoodReq foodReq) {
        try {
            Optional<Food> food = foodRepository.findById(id);
            if (food.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Food notfound")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());

            food.get().setName(foodReq.getName());
            food.get().setPrice(foodReq.getPrice());
            food.get().setFoodType(FoodType.valueOf(foodReq.getFoodType()));

            Food foodRes = foodRepository.save(food.get());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Update food success")
                            .result(foodRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Internal Server Error")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> getFoods(String type) {
        try {
            List<Food> foods;
            if (type.isEmpty()){
                foods = foodRepository.findAll();
            }else {
                FoodType foodType = FoodType.valueOf(type);
                foods = foodRepository.findAllByFoodType(foodType);
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Get food success")
                            .result(foods)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Internal Server Error")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
}
