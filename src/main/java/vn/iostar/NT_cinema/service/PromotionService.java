package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.DiscountType;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.PromotionCodeReq;
import vn.iostar.NT_cinema.dto.PromotionFixedReq;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.PromotionCodeRepository;
import vn.iostar.NT_cinema.repository.PromotionCodeUsageRepository;
import vn.iostar.NT_cinema.repository.PromotionFixedRepository;
import vn.iostar.NT_cinema.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class PromotionService {
    @Autowired
    PromotionFixedRepository promotionFixedRepository;
    @Autowired
    PromotionCodeRepository promotionCodeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    PromotionCodeUsageRepository promotionCodeUsageRepository;

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
            if (promotionFixedReq.getNormalValue() == null || promotionFixedReq.getVipValue() == null || promotionFixedReq.getCoupleValue() == null) {
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
            promotionFixed.setNormalValue(promotionFixedReq.getNormalValue());
            promotionFixed.setVipValue(promotionFixedReq.getVipValue());
            promotionFixed.setCoupleValue(promotionFixedReq.getCoupleValue());
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
                promotion.get().setNormalValue(promotionFixedReq.getNormalValue());
                promotion.get().setVipValue(promotionFixedReq.getVipValue());
                promotion.get().setCoupleValue(promotionFixedReq.getCoupleValue());
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
                promotion.get().setDeleted(!promotion.get().isDeleted());
                promotionFixedRepository.save(promotion.get());

                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message(promotion.get().isDeleted() ? "Khuyến mãi đã được xóa!" : "Khuyến mãi đã được khởi động lại!")
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
                promotionCode.setMaxDiscountAmount(promotionCodeReq.getMaxDiscountAmount());
                promotionCode.setMinOrderValue(promotionCodeReq.getMinOrderValue());
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

    public ResponseEntity<GenericResponse> updatePromotionCode(String id, PromotionCodeReq promotionCodeReq) {
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
                promotion.get().setMaxDiscountAmount(promotionCodeReq.getMaxDiscountAmount());
                promotion.get().setMinOrderValue(promotionCodeReq.getMinOrderValue());
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
                promotion.get().setDeleted(!promotion.get().isDeleted());
                promotionCodeRepository.save(promotion.get());

                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message(promotion.get().isDeleted() ? "Khuyến mãi đã được xóa!" : "Khuyến mãi đã được khởi động lại!")
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

    public ResponseEntity<GenericResponse> getPromotion(String id) {
        try {
            Optional<PromotionCode> promotion = promotionCodeRepository.findById(id);
            if (promotion.isPresent()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Lấy thông tin khuyến mãi thành công.")
                                .result(promotion.get())
                                .statusCode(HttpStatus.OK.value())
                                .build());
            } else {
                Optional<PromotionFixed> promotionFixed = promotionFixedRepository.findById(id);
                return promotionFixed.map(fixed -> ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Lấy thông tin khuyến mãi thành công.")
                                .result(fixed)
                                .statusCode(HttpStatus.OK.value())
                                .build())).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Khuyến mái không tồn tại!")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build()));
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

    public List<PromotionFixed> listPromotionFixedAvailable(Booking booking) {
        Criteria criteria = Criteria.where("isDeleted").is(false)
                .and("isValid").is(true);
        criteria.andOperator(
                Criteria.where("startDate").lte(booking.getSeats().get(0).getSchedule().getDate()),
                Criteria.where("endDate").gte(booking.getSeats().get(0).getSchedule().getDate())
        );
        Query query = Query.query(criteria);
        List<PromotionFixed> promotionFixedList = mongoTemplate.find(query, PromotionFixed.class);
        promotionFixedList.removeIf(item -> !checkPromotionFixed(item, booking));
        return promotionFixedList;
    }

    public boolean checkPromotionFixed(PromotionFixed promotion, Booking booking) {
        if (promotion.getValidDayOfWeek() != null && promotion.getValidDayOfWeek() != booking.getSeats().get(0).getSchedule().getDate().getDayOfWeek().getValue()) {
            return false;
        }
        LocalTime localTime = booking.getSeats().get(0).getSchedule().getStartTime();
        if (promotion.getValidTimeFrameStart() != null && promotion.getValidTimeFrameEnd() != null) {
            return !localTime.isBefore(promotion.getValidTimeFrameStart()) && !localTime.isAfter(promotion.getValidTimeFrameEnd());
        }
        Optional<User> user = userRepository.findById(booking.getUserId());
        if (user.isPresent()) {
            if (user.get().getDob() != null) {
                LocalDate date = user.get().getDob().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return promotion.getAgeLimit() >= LocalDate.now().getYear() - date.getYear() + 1;
            }
        }
        return true;
    }

    public Map<Boolean, String> checkPromotionCode(PromotionCode promotion, Booking booking) {
        Map<Boolean, String> map = new HashMap<>();
        if (promotion.getMaxUsage() <= 0) {
            map.put(false, "Khuyến mãi đã hết lượt sử dụng!");
            return map;
        }
        if (promotion.getStartDate().isAfter(LocalDate.now()) || promotion.getEndDate().isBefore(LocalDate.now())) {
            map.put(false, "Khuyến mãi đã hết hạn sử dụng!");
            return map;
        }
        if (promotion.getMinOrderValue().compareTo(booking.getTotal()) > 0) {
            map.put(false, "Mã khuyến mãi không áp dụng cho đơn hàng dưới "+ promotion.getMinOrderValue() +" VND.");
            return map;
        }
        List<PromotionCodeUsage> promotionCodeUsages = promotionCodeUsageRepository.findAllByUserIdAndPromotionCodeIdAndDateUsed(booking.getUserId(), promotion.getPromotionCodeId(), LocalDate.now());
        if (promotion.getUseForUserPerDay() < promotionCodeUsages.size()) {
            map.put(false, "Khuyến mãi đã sử dụng quá "+ promotion.getUseForUserPerDay() +" lần trong một ngày!");
            return map;
        }
        map.put(true, "Khuyến mãi đủ điểu kiện.");
        return map;
    }

    public BigDecimal calculateTotal(Booking booking, PromotionCode promotion) {
        BigDecimal total = booking.getTotal();
        if (promotion.getDiscountType().equals(DiscountType.FIXED_AMOUNT)){
            total = total.subtract(promotion.getDiscountValue());
        }
        if (promotion.getDiscountType().equals(DiscountType.PERCENTAGE)){
            if (total.multiply(promotion.getDiscountValue()).compareTo(promotion.getMaxDiscountAmount()) > 0){
                total = total.subtract(promotion.getMaxDiscountAmount());
            }else
                total = total.subtract(total.multiply(promotion.getDiscountValue()));
        }
        return total;
    }

    public ResponseEntity<GenericResponse> createPromotionCodeUsage() {
        try {
            PromotionCodeUsage usage = new PromotionCodeUsage();
            usage.setPromotionCodeId("1");
            usage.setUserId("1");
            usage.setDateUsed(LocalDate.now());
            promotionCodeUsageRepository.save(usage);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Đã tạo đơn đặt vé!")
                            .result(null)
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
}
