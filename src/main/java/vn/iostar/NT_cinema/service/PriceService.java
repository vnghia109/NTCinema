package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.PriceType;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.PriceReq;
import vn.iostar.NT_cinema.entity.Price;
import vn.iostar.NT_cinema.repository.PriceRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PriceService {
    @Autowired
    PriceRepository priceRepository;

    public ResponseEntity<GenericResponse> getAllPrice() {
        List<Price> prices = priceRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK)
                .body(GenericResponse.builder()
                        .success(true)
                        .message("Thành công!")
                        .result(prices)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }


    public ResponseEntity<GenericResponse> updatePrice(String id, PriceReq priceReq) {
        try {
            Optional<Price> price = priceRepository.findById(id);
            if (price.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Giá không tồn tại.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            price.get().setPrice(priceReq.getPrice());
            price.get().setType(PriceType.valueOf(priceReq.getType()));

            Price price1 = priceRepository.save(price.get());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Thành công!")
                            .result(price1)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<?> getPriceOfSeat(String type) {
        try {
            Optional<Price> price = priceRepository.findByType(PriceType.valueOf(type));
            return price.map(value -> ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy giá vé thành công!")
                            .result(value)
                            .statusCode(HttpStatus.OK.value())
                            .build())).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Giá vé không tồn tại!")
                            .result(null)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .build()));
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
