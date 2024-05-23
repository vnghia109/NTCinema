package vn.iostar.NT_cinema.service;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;
import de.jollyday.parameter.CalendarPartManagerParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.DiscountType;
import vn.iostar.NT_cinema.dto.ApplyPromotion;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.PromotionCodeReq;
import vn.iostar.NT_cinema.dto.PromotionFixedReq;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.PromotionCodeRepository;
import vn.iostar.NT_cinema.repository.PromotionFixedRepository;
import vn.iostar.NT_cinema.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PromotionService {
    @Autowired
    PromotionFixedRepository promotionFixedRepository;
    @Autowired
    PromotionCodeRepository promotionCodeRepository;
    @Autowired
    UserRepository userRepository;

    public ResponseEntity<GenericResponse> getAllPromotions() {
        try {
            List<PromotionFixed> promotionFixeds = promotionFixedRepository.findAll();

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy danh sách khuyến mãi thành công!")
                            .result(promotionFixeds)
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

    public ResponseEntity<GenericResponse> createPromotionFixed(PromotionFixedReq promotionFixedReq) {
        try {
            PromotionFixed promotionFixed = new PromotionFixed();
            promotionFixed.setName(promotionFixedReq.getName());
            promotionFixed.setDescription(promotionFixedReq.getDescription());
            promotionFixed.setDiscountType(DiscountType.valueOf(promotionFixedReq.getDiscountType()));
            promotionFixed.setDiscountValue(promotionFixedReq.getDiscountValue());
            promotionFixed.setValidDayOfWeek(promotionFixedReq.getValidDayOfWeek());
            promotionFixed.setAgeLimit(promotionFixedReq.getAgeLimit());
            promotionFixed.setValidTimeFrameStart(promotionFixedReq.getValidTimeFrameStart());
            promotionFixed.setValidTimeFrameEnd(promotionFixedReq.getValidTimeFrameEnd());
            promotionFixed.setStartDate(promotionFixedReq.getStartDate());
            promotionFixed.setEndDate(promotionFixedReq.getEndDate());

            PromotionFixed response = promotionFixedRepository.save(promotionFixed);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Một khuyến mãi cố định đã được thêm!")
                            .result(response)
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

    public ResponseEntity<GenericResponse> updatePromotionFixed(String id, PromotionFixedReq promotionFixedReq) {
        try {
            Optional<PromotionFixed> promotion = promotionFixedRepository.findById(id);
            if (promotion.isPresent()) {
                promotion.get().setName(promotionFixedReq.getName());
                promotion.get().setDescription(promotionFixedReq.getDescription());
                promotion.get().setDiscountType(DiscountType.valueOf(promotionFixedReq.getDiscountType()));
                promotion.get().setDiscountValue(promotionFixedReq.getDiscountValue());
                promotion.get().setValidDayOfWeek(promotionFixedReq.getValidDayOfWeek());
                promotion.get().setAgeLimit(promotionFixedReq.getAgeLimit());
                promotion.get().setValidTimeFrameStart(promotionFixedReq.getValidTimeFrameStart());
                promotion.get().setValidTimeFrameEnd(promotionFixedReq.getValidTimeFrameEnd());
                promotion.get().setStartDate(promotionFixedReq.getStartDate());
                promotion.get().setEndDate(promotionFixedReq.getEndDate());

                promotionFixedRepository.save(promotion.get());
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

    public ResponseEntity<GenericResponse> deletePromotionFixed(String id) {
        try {
            Optional<PromotionFixed> promotion = promotionFixedRepository.findById(id);
            if (promotion.isPresent()) {
                promotion.get().setDeleted(true);
                promotionFixedRepository.save(promotion.get());

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

    public ResponseEntity<GenericResponse> createPromotionCode(PromotionCodeReq promotionCodeReq) {
        try {
            PromotionCode promotionCode = new PromotionCode();
            promotionCode.setName(promotionCodeReq.getName());
            promotionCode.setDescription(promotionCodeReq.getDescription());
            promotionCode.setDiscountType(DiscountType.valueOf(promotionCodeReq.getDiscountType()));
            promotionCode.setDiscountValue(promotionCodeReq.getDiscountValue());
            promotionCode.setPromotionCode(promotionCodeReq.getPromotionCode());
            promotionCode.setMaxUsage(promotionCodeReq.getMaxUsage());
            promotionCode.setUseForUserPerDay(promotionCodeReq.getUseForUserPerDay());
            promotionCode.setStartDate(promotionCodeReq.getStartDate());
            promotionCode.setEndDate(promotionCodeReq.getEndDate());
            promotionCode.setCreateAt(LocalDate.now());

            PromotionCode response = promotionCodeRepository.save(promotionCode);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Một khuyến mãi cố định đã được thêm!")
                            .result(response)
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

    public ResponseEntity<GenericResponse> updatePromotionFixedCode(String id, PromotionCodeReq promotionCodeReq) {
        try {
            Optional<PromotionCode> promotion = promotionCodeRepository.findById(id);
            if (promotion.isPresent()) {
                promotion.get().setName(promotionCodeReq.getName());
                promotion.get().setDescription(promotionCodeReq.getDescription());
                promotion.get().setDiscountType(DiscountType.valueOf(promotionCodeReq.getDiscountType()));
                promotion.get().setDiscountValue(promotionCodeReq.getDiscountValue());
                promotion.get().setPromotionCode(promotionCodeReq.getPromotionCode());
                promotion.get().setMaxUsage(promotionCodeReq.getMaxUsage());
                promotion.get().setUseForUserPerDay(promotionCodeReq.getUseForUserPerDay());
                promotion.get().setStartDate(promotionCodeReq.getStartDate());
                promotion.get().setEndDate(promotionCodeReq.getEndDate());

                promotionCodeRepository.save(promotion.get());
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

    public ResponseEntity<GenericResponse> deletePromotionCode(String id) {
        try {
            Optional<PromotionCode> promotion = promotionCodeRepository.findById(id);
            if (promotion.isPresent()) {
                promotion.get().setDeleted(true);
                promotionCodeRepository.save(promotion.get());

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
