package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.repository.FoodInventoryRepository;

@Service
public class FoodInventoryService {
    @Autowired
    FoodInventoryRepository foodInventoryRepository;
}
