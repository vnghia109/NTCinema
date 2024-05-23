package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.DiscountType;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.PromotionCodeReq;
import vn.iostar.NT_cinema.dto.PromotionFixedReq;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.PromotionCodeRepository;
import vn.iostar.NT_cinema.repository.PromotionFixedRepository;
import vn.iostar.NT_cinema.repository.UserRepository;

import java.time.LocalDate;
import java.util.*;

@Service
public class PromotionService {
    @Autowired
    PromotionFixedRepository promotionFixedRepository;
    @Autowired
    PromotionCodeRepository promotionCodeRepository;
    @Autowired
    UserRepository userRepository;

    public ResponseEntity<GenericResponse> getAllPromotions(boolean isFixed, String code, Pageable pageable) {
        try {
            if (isFixed) {
                Page<PromotionFixed> promotionFixeds = promotionFixedRepository.findAll(pageable);
                Map<String, Object> map = new HashMap<>();
                map.put("content", promotionFixeds.getContent());
                map.put("pageNumber", promotionFixeds.getPageable().getPageNumber() + 1);
                map.put("pageSize", promotionFixeds.getSize());
                map.put("totalPages", promotionFixeds.getTotalPages());
                map.put("totalElements", promotionFixeds.getTotalElements());

                return ResponseEntity.ok(
                        GenericResponse.builder()
                                .success(true)
                                .message("Lấy danh sách khuyến mãi thành công!")
                                .result(map)
                                .statusCode(HttpStatus.OK.value())
                                .build()
                );
            }else {
                Page<PromotionCode> promotionCodes;
                if (code != null) {
                    promotionCodes = promotionCodeRepository.findAllByPromotionCode(code, pageable);
                }else {
                    promotionCodes = promotionCodeRepository.findAll(pageable);
                }

                Map<String, Object> map = new HashMap<>();
                map.put("content", promotionCodes.getContent());
                map.put("pageNumber", promotionCodes.getPageable().getPageNumber() + 1);
                map.put("pageSize", promotionCodes.getSize());
                map.put("totalPages", promotionCodes.getTotalPages());
                map.put("totalElements", promotionCodes.getTotalElements());

                return ResponseEntity.ok(
                        GenericResponse.builder()
                                .success(true)
                                .message("Lấy danh sách khuyến mãi thành công!")
                                .result(map)
                                .statusCode(HttpStatus.OK.value())
                                .build()
                );
            }

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
            if (promotionFixedReq.getName() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Vui lòng nhập tên khuyến mãi!")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .build());
            }
            if (promotionFixedReq.getDiscountType() == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Vui lòng chọn loại khuyến mãi!")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .build());
            }
            if (promotionFixedReq.getDiscountValue() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Vui lòng nhập giá trị của khuyến mãi!")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .build());
            }
            if(promotionFixedReq.getStartDate() == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Vui lòng chọn ngày bắt đầu của khuyến mãi!")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .build());
            }
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
            promotionFixed.setCreateAt(LocalDate.now());

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
            if (promotionCodeRepository.existsByPromotionCode(promotionCodeReq.getPromotionCode())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Mã khuyến mãi đã đưọc sử dụng!")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .build());
            }else {
                if (promotionCodeReq.getName() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(GenericResponse.builder()
                                    .success(false)
                                    .message("Vui lòng nhập tên khuyến mãi!")
                                    .result(null)
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                                    .build());
                }
                if (promotionCodeReq.getDiscountType() == null){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(GenericResponse.builder()
                                    .success(false)
                                    .message("Vui lòng chọn loại khuyến mãi!")
                                    .result(null)
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                                    .build());
                }
                if (promotionCodeReq.getDiscountValue() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(GenericResponse.builder()
                                    .success(false)
                                    .message("Vui lòng nhập giá trị của khuyến mãi!")
                                    .result(null)
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                                    .build());
                }
                if(promotionCodeReq.getStartDate() == null){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(GenericResponse.builder()
                                    .success(false)
                                    .message("Vui lòng chọn ngày bắt đầu của khuyến mãi!")
                                    .result(null)
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                                    .build());
                }
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
                                .message("Một khuyến mãi nhập code đã được thêm!")
                                .result(response)
                                .statusCode(HttpStatus.OK.value())
                                .build());
        }
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
