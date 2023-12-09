package vn.iostar.NT_cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.service.FoodService;

@RestController
@RequestMapping("/api/v1/foods")
public class FoodController {
    @Autowired
    FoodService foodService;
    @GetMapping("")
    public ResponseEntity<GenericResponse> getFoods(@RequestParam(defaultValue = "") String type){
        return foodService.getFoods(type);
    }
}
