package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.DiscountType;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.PromotionReq;
import vn.iostar.NT_cinema.entity.Promotion;
import vn.iostar.NT_cinema.repository.PromotionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PromotionService {
    @Autowired
    PromotionRepository promotionRepository;

    public ResponseEntity<GenericResponse> getAllPromotions() {
        try {
            List<Promotion> promotions = promotionRepository.findAll();

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy danh sách khuyến mãi thành công!")
                            .result(promotions)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> createPromotion(PromotionReq promotionReq) {
        try {
            Promotion promotion = new Promotion();
            promotion.setName(promotionReq.getName());
            promotion.setDescription(promotionReq.getDescription());
            promotion.setDiscountType(DiscountType.valueOf(promotionReq.getDiscountType()));
            promotion.setDiscountValue(promotionReq.getDiscountValue());
            promotion.setValidDayOfWeek(promotionReq.getValidDayOfWeek());
            promotion.setAgeLimit(promotionReq.getAgeLimit());
            promotion.setValidTimeFrameStart(promotionReq.getValidTimeFrameStart());
            promotion.setValidTimeFrameEnd(promotionReq.getValidTimeFrameEnd());
            promotion.setExcludeHolidays(promotionReq.isExcludeHolidays());
            promotion.setStartDate(promotionReq.getStartDate());
            promotion.setEndDate(promotionReq.getEndDate());

            promotionRepository.save(promotion);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Một khuyến mãi đã được thêm!")
                            .result(promotion)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> updatePromotion(String id, PromotionReq promotionReq) {
        try {
            Optional<Promotion> promotion = promotionRepository.findById(id);
            if (promotion.isPresent()) {
                promotion.get().setName(promotionReq.getName());
                promotion.get().setDescription(promotionReq.getDescription());
                promotion.get().setDiscountType(DiscountType.valueOf(promotionReq.getDiscountType()));
                promotion.get().setDiscountValue(promotionReq.getDiscountValue());
                promotion.get().setValidDayOfWeek(promotionReq.getValidDayOfWeek());
                promotion.get().setAgeLimit(promotionReq.getAgeLimit());
                promotion.get().setValidTimeFrameStart(promotionReq.getValidTimeFrameStart());
                promotion.get().setValidTimeFrameEnd(promotionReq.getValidTimeFrameEnd());
                promotion.get().setExcludeHolidays(promotionReq.isExcludeHolidays());
                promotion.get().setStartDate(promotionReq.getStartDate());
                promotion.get().setEndDate(promotionReq.getEndDate());

                promotionRepository.save(promotion.get());
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Một khuyến mãi đã được cập nhật!")
                                .result(promotion.get())
                                .statusCode(HttpStatus.OK.value())
                                .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Khuyến mãi không tồn tại!")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result("Lỗi máy chủ.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> deletePromotion(String id) {
        try {
            Optional<Promotion> promotion = promotionRepository.findById(id);
            if (promotion.isPresent()) {
                promotion.get().setDeleted(true);
                promotionRepository.save(promotion.get());

                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Một khuyến mãi đã được xóa!")
                                .result(null)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Khuyến mãi không tồn tại!")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
        }catch (Exception e) {
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
