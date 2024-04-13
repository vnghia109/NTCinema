package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.repository.PromotionRepository;

@Service
public class PromotionService {
    @Autowired
    PromotionRepository promotionRepository;
}
