package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.StockEntryReq;
import vn.iostar.NT_cinema.dto.StockEntryRes;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.exception.NotFoundException;
import vn.iostar.NT_cinema.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    CinemaFinanceStatsRepository cinemaFinanceStatsRepository;
    @Autowired
    CinemaRepository cinemaRepository;
    @Autowired
    RoleRepository roleRepository;

    public ResponseEntity<GenericResponse> importFoods(String managerId, StockEntryReq req) {
        try {
            Optional<Manager> manager = managerRepository.findById(managerId);
            if (manager.isEmpty())
                throw new NotFoundException("Quản lý không tồn tại. Vui lòng đăng nhập lại.");
            Optional<Food> food = foodRepository.findById(req.getFoodId());
            if (food.isEmpty())
                throw new NotFoundException("Mặt hàng này không tồn tại.");
            if (req.getTotalPrice() != req.getQuantity() * req.getPurchasePrice())
                throw new IllegalArgumentException("Tổng tiền không đúng với số lượng và đơn giá.");
            StockEntry stockEntry = new StockEntry();
            stockEntry.setManager(manager.get());
            stockEntry.setFood(food.get());
            stockEntry.setQuantity(req.getQuantity());
            stockEntry.setPurchasePrice(req.getPurchasePrice());
            stockEntry.setEntryDate(new Date());
            stockEntry.setSupplier(req.getSupplier());
            stockEntry.setTotalPrice(req.getQuantity() * req.getPurchasePrice());

            StockEntry saved = stockEntryRepository.save(stockEntry);
            handleImportFoods(saved, manager.get().getCinema());

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
            throw new RuntimeException(e.getMessage());
        }
    }

    public void handleImportFoods(StockEntry stockEntry, Cinema cinema) {
        LocalDate date = stockEntry.getEntryDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1);
        Optional<CinemaFinanceStats> financeStats = cinemaFinanceStatsRepository.findByCinemaAndMonth(
                cinema,
                date
        );
        if (financeStats.isPresent()) {
            financeStats.get().setTotalExpense(financeStats.get().getTotalExpense().add(BigDecimal.valueOf(stockEntry.getTotalPrice())));
            financeStats.get().setFoodExpense(financeStats.get().getFoodExpense().add(BigDecimal.valueOf(stockEntry.getTotalPrice())));
            financeStats.get().setTotalOfOrder(financeStats.get().getTotalOfOrder() + 1);
            financeStats.get().calculateProfit();
            cinemaFinanceStatsRepository.save(financeStats.get());
        }else {
            CinemaFinanceStats cinemaFinanceStats = new CinemaFinanceStats(
                    date,
                    cinema,
                    BigDecimal.valueOf(stockEntry.getTotalPrice()),
                    BigDecimal.valueOf(stockEntry.getTotalPrice()),
                    1);
            cinemaFinanceStats.calculateProfit();
            cinemaFinanceStatsRepository.save(cinemaFinanceStats);
        }
    }

    public ResponseEntity<GenericResponse> getStockEntriesByCinema(Pageable pageable, String cinemaId){
        try {
            Optional<Cinema> cinema = cinemaRepository.findById(cinemaId);
            Role role = roleRepository.findByRoleName("MANAGER");
            if (cinema.isEmpty())
                throw new NotFoundException("Rạp phim không tìm thấy.");
            Optional<Manager> manager = managerRepository.findByCinemaAndRole(cinema.get(), role);
            if (manager.isEmpty())
                throw new NotFoundException("Quản lý không tồn tại. Vui lòng đăng nhập lại.");
            return getStockEntries(pageable, manager.get().getUserId());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> getStockEntries(Pageable pageable, String managerId) {
        try {
            Optional<Manager> manager = managerRepository.findById(managerId);
            Page<StockEntry> stockEntries;
            if (manager.isEmpty())
                stockEntries = stockEntryRepository.findAll(pageable);
            else
                stockEntries = stockEntryRepository.findAllByManager(manager.get(), pageable);
            Map<String, Object> result = new HashMap<>();
            result.put("content", stockEntries.getContent().stream().sorted(Comparator.comparing(StockEntry::getStockEntryId).reversed()).collect(Collectors.toList()));
            result.put("pageNumber", stockEntries.getPageable().getPageNumber()+1);
            result.put("pageSize", stockEntries.getSize());
            result.put("totalPages", stockEntries.getTotalPages());
            result.put("totalElements", stockEntries.getTotalElements());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy danh sách nhập hàng thành công.")
                            .result(result)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
