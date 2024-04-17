package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.StockEntryReq;
import vn.iostar.NT_cinema.dto.StockEntryRes;
import vn.iostar.NT_cinema.entity.Food;
import vn.iostar.NT_cinema.entity.FoodInventory;
import vn.iostar.NT_cinema.entity.Manager;
import vn.iostar.NT_cinema.entity.StockEntry;
import vn.iostar.NT_cinema.repository.FoodInventoryRepository;
import vn.iostar.NT_cinema.repository.FoodRepository;
import vn.iostar.NT_cinema.repository.ManagerRepository;
import vn.iostar.NT_cinema.repository.StockEntryRepository;

import java.util.Date;
import java.util.Optional;

@Service
public class StockEntryService {
    @Autowired
    StockEntryRepository stockEntryRepository;
    @Autowired
    ManagerRepository managerRepository;
    @Autowired
    FoodRepository foodRepository;
    @Autowired
    FoodInventoryRepository foodInventoryRepository;

    public ResponseEntity<GenericResponse> importFoods(String managerId, StockEntryReq req) {
        try {
            Optional<Manager> manager = managerRepository.findById(managerId);
            if (manager.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Quản lý không tồn tại. Vui lòng đăng nhập lại.")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .build());
            }
            Optional<Food> food = foodRepository.findById(req.getFoodId());
            if (food.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Mặt hàng này không tồn tại.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            if (req.getTotalPrice() != req.getQuantity() * req.getPurchasePrice()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Tổng tiền không đúng với số lượng và đơn giá.")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .build());
            }
            StockEntry stockEntry = new StockEntry();
            stockEntry.setManager(manager.get());
            stockEntry.setFood(food.get());
            stockEntry.setQuantity(req.getQuantity());
            stockEntry.setPurchasePrice(req.getPurchasePrice());
            stockEntry.setEntryDate(new Date());
            stockEntry.setSupplier(req.getSupplier());
            stockEntry.setTotalPrice(req.getQuantity() * req.getPurchasePrice());

            StockEntry saved = stockEntryRepository.save(stockEntry);

            Optional<FoodInventory> inventory = foodInventoryRepository.findByFoodAndCinema(food.get(), manager.get().getCinema());
            if (inventory.isEmpty()) {
                FoodInventory foodInventory = new FoodInventory();
                foodInventory.setFood(food.get());
                foodInventory.setCinema(manager.get().getCinema());
                foodInventory.setQuantity(saved.getQuantity());
                foodInventory.setUpdateAt(new Date());
                foodInventoryRepository.save(foodInventory);
            } else {
                FoodInventory foodInventory = inventory.get();
                foodInventory.setQuantity(foodInventory.getQuantity() + saved.getQuantity());
                foodInventoryRepository.save(foodInventory);
            }

            food.get().setQuantity(food.get().getQuantity() + saved.getQuantity());
            foodRepository.save(food.get());

            StockEntryRes res = new StockEntryRes(saved.getStockEntryId(), saved.getFood(), saved.getManager().getUserId(), saved.getQuantity(), saved.getPurchasePrice(), saved.getEntryDate(), saved.getSupplier(), saved.getTotalPrice());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Nhập hàng thành công!")
                            .result(res)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
}
