package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    CloudinaryService cloudinaryService;

    public ResponseEntity<GenericResponse> addFood(FoodReq foodReq) {
        try {
            Food food = new Food();
            food.setName(foodReq.getName());
            food.setPrice(foodReq.getPrice());
            try {
                food.setFoodType(FoodType.valueOf(foodReq.getFoodType()));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Thể loại đồ ăn không tồn tại.!")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .build());
            }

            String image = cloudinaryService.uploadImage(foodReq.getImage());
            food.setImage(image);
            food.setQuantity(0);

            Food foodRes = foodRepository.save(food);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Thêm thức ăn thành công!")
                            .result(foodRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ")
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
                                .message("Không tìm thấy đồ ăn.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());

            foodRepository.delete(food.get());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Xóa đồ ăn thành công!")
                            .result(null)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
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
                                .message("Không tìm thấy đồ ăn.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());

            food.get().setStatus(!food.get().isStatus());

            Food foodRes = foodRepository.save(food.get());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Cập nhật trạng thái đồ ăn thành công!")
                            .result(foodRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
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
                                .message("Không tìm thấy đồ ăn.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());

            food.get().setName(foodReq.getName());
            food.get().setPrice(foodReq.getPrice());
            food.get().setFoodType(FoodType.valueOf(foodReq.getFoodType()));

            cloudinaryService.deleteImage(food.get().getImage());
            String image = cloudinaryService.uploadImage(foodReq.getImage());
            food.get().setImage(image);

            Food foodRes = foodRepository.save(food.get());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Cập nhật đồ ăn thành công!")
                            .result(foodRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> getFoods(String type, Pageable pageable) {
        try {
            Page<Food> foods;
            if (type.isEmpty()){
                foods = foodRepository.findAll(pageable);
            }else {
                FoodType foodType = FoodType.valueOf(type);
                foods = foodRepository.findAllByFoodType(foodType, pageable);
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy thông tin đồ ăn thành công!")
                            .result(foods)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<?> getFood(String foodId) {
        try {
            Optional<Food> foodOptional = foodRepository.findById(foodId);
            return foodOptional.map(food -> ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy thông tin đồ ăn thành công!")
                            .result(food)
                            .statusCode(HttpStatus.OK.value())
                            .build())).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Không tìm thấy đồ ăn.")
                            .result(null)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .build()));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
}
