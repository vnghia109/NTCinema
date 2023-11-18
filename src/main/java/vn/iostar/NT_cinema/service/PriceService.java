package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.PriceType;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.PriceReq;
import vn.iostar.NT_cinema.entity.Price;
import vn.iostar.NT_cinema.repository.PriceRepository;

import java.util.List;

@Service
public class PriceService {
    @Autowired
    PriceRepository priceRepository;

    public ResponseEntity<GenericResponse> getAllPrice() {
        List<Price> prices = priceRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK)
                .body(GenericResponse.builder()
                        .success(true)
                        .message("Success")
                        .result(prices)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    public ResponseEntity<GenericResponse> addPrice(PriceReq priceReq) {
        try {
            Price price = new Price();
            price.setPrice(priceReq.getPrice());
            price.setType(PriceType.valueOf(priceReq.getType()));

            Price price1 = priceRepository.save(price);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Success")
                            .result(price1)
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
