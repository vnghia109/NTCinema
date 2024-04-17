package vn.iostar.NT_cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.service.FoodService;

@RestController
@RequestMapping("/api/v1/foods")
public class FoodController {
    @Autowired
    FoodService foodService;
    @GetMapping("")
    public ResponseEntity<GenericResponse> getFoods(@RequestParam(defaultValue = "") String type,
                                                    @RequestParam(defaultValue = "1") int index,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam String cinemaId) {
        return foodService.getFoods(type, cinemaId, PageRequest.of(index-1, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFood(@PathVariable("id") String foodId){
        return foodService.getFood(foodId);
    }
}
