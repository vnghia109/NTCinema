package vn.iostar.NT_cinema.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.DiscountType;
import vn.iostar.NT_cinema.controller.util.PaginationUtils;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.PromotionCodeReq;
import vn.iostar.NT_cinema.dto.PromotionFixedReq;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.exception.NotFoundException;
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
    @Autowired
    NotificationService notificationService;
    @Autowired
    CloudinaryService cloudinaryService;

    public ResponseEntity<GenericResponse> getAllPromotions(boolean isFixed, String code, String name, Pageable pageable) {
        try {
            Map<String, Object> map = new HashMap<>();
            if (isFixed) {
                List<PromotionFixed> promotionFixeds = promotionFixedRepository.findAll();
                if (name != null && !name.isBlank()) {
                    promotionFixeds = promotionFixeds.stream()
                            .filter(promotionFixed -> promotionFixed.getName().toLowerCase().contains(name.toLowerCase())).toList();
                }
                Page<PromotionFixed> result = PaginationUtils.paginate(promotionFixeds, pageable);

                map.put("content", result.getContent());
                map.put("pageNumber", result.getPageable().getPageNumber() + 1);
                map.put("pageSize", result.getSize());
                map.put("totalPages", result.getTotalPages());
                map.put("totalElements", result.getTotalElements());
            }else {
                List<PromotionCode> promotionCodes;
                if (code != null) {
                    promotionCodes = promotionCodeRepository.findAllByPromotionCode(code);
                }else {
                    promotionCodes = promotionCodeRepository.findAll();
                }
                if (name != null && !name.isBlank()) {
                    promotionCodes = promotionCodes.stream()
                            .filter(promotionFixed -> promotionFixed.getName().toLowerCase().contains(name.toLowerCase())).toList();
                }
                Page<PromotionCode> result = PaginationUtils.paginate(promotionCodes, pageable);

                map.put("content", result.getContent());
                map.put("pageNumber", result.getPageable().getPageNumber() + 1);
                map.put("pageSize", result.getSize());
                map.put("totalPages", result.getTotalPages());
                map.put("totalElements", result.getTotalElements());
            }
            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Lấy danh sách khuyến mãi thành công!")
                            .result(map)
                            .statusCode(HttpStatus.OK.value())
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> createPromotionFixed(PromotionFixedReq promotionFixedReq) {
        try {
            PromotionFixed promotionFixed = new PromotionFixed();
            if (promotionFixedReq.getName() == null)
                throw new IllegalArgumentException("Vui lòng nhập tên khuyến mãi!");

            if (promotionFixedReq.getNormalValue() == null || promotionFixedReq.getVipValue() == null || promotionFixedReq.getCoupleValue() == null)
                throw new IllegalArgumentException("Vui lòng nhập giá trị của khuyến mãi!");

            if(promotionFixedReq.getStartDate() == null)
                throw new IllegalArgumentException("Vui lòng chọn ngày bắt đầu của khuyến mãi!");

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
            promotionFixed.setImage(cloudinaryService.uploadImage(promotionFixedReq.getImage()));

            PromotionFixed response = promotionFixedRepository.save(promotionFixed);
            changeValidPromotion();
            notificationService.promotionNotification(response);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Một khuyến mãi cố định đã được thêm!")
                            .result(response)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
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

                cloudinaryService.deleteImage(promotion.get().getImage());
                promotion.get().setImage(cloudinaryService.uploadImage(promotionFixedReq.getImage()));

                changeValidPromotion();
                promotionFixedRepository.save(promotion.get());
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Một khuyến mãi đã được cập nhật!")
                                .result(promotion.get())
                                .statusCode(HttpStatus.OK.value())
                                .build());
            } else {
                throw new NotFoundException("Khuyến mãi không tồn tại!");
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
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
                throw new NotFoundException("Khuyến mãi không tồn tại!");
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> createPromotionCode(PromotionCodeReq promotionCodeReq) {
        try {
            if (promotionCodeRepository.existsByPromotionCode(promotionCodeReq.getPromotionCode())) {
                throw new IllegalArgumentException("Mã khuyến mãi đã được sử dụng!");
            }else {
                if (promotionCodeReq.getName() == null) {
                    throw new IllegalArgumentException("Vui lòng nhập tên khuyến mãi!");
                }
                if (promotionCodeReq.getDiscountType() == null){
                    throw new IllegalArgumentException("Vui lòng chọn loại khuyến mãi!");
                }
                if (promotionCodeReq.getDiscountValue() == null) {
                    throw new IllegalArgumentException("Vui lòng nhập giá trị của khuyến mãi!");
                }
                if(promotionCodeReq.getStartDate() == null){
                    throw new IllegalArgumentException("Vui lòng chọn ngày bắt đầu của khuyến mãi!");
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
                promotionCode.setImage(cloudinaryService.uploadImage(promotionCodeReq.getImage()));

                changeValidPromotion();
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
            throw new RuntimeException(e.getMessage());
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

                cloudinaryService.deleteImage(promotion.get().getImage());
                promotion.get().setImage(cloudinaryService.uploadImage(promotionCodeReq.getImage()));

                changeValidPromotion();
                promotionCodeRepository.save(promotion.get());
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Một khuyến mãi đã được cập nhật!")
                                .result(promotion.get())
                                .statusCode(HttpStatus.OK.value())
                                .build());
            } else {
                throw new NotFoundException("Khuyến mãi không tồn tại!");
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
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
                throw new NotFoundException("Khuyến mãi không tồn tại!");
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
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
            throw new RuntimeException(e.getMessage());
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
        if (promotion.getAgeLimit() != null && promotion.getAgeLimit() != 0) {
            Optional<User> user = userRepository.findById(booking.getUserId());
            if (user.isPresent()) {
                if (user.get().getDob() != null) {
                    LocalDate date = user.get().getDob().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    System.out.println(user.get().getUserId() + " " + date);
                    return promotion.getAgeLimit() >= LocalDate.now().getYear() - date.getYear() + 1;
                }
            }else {
                return false;
            }
        }
        return true;
    }

    public Map<Boolean, String> checkPromotionCode(PromotionCode promotion, Booking booking) {
        Map<Boolean, String> map = new HashMap<>();
        if (promotion.isDeleted()){
            map.put(false, "Khuyến mãi đá xóa!");
            return map;
        }
        if (!promotion.isValid()) {
            map.put(false, "Khuyến mãi đã hết hạn sử dụng!");
            return map;
        }
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

    @Scheduled(cron = "0 0 5 * * *", zone = "GMT+7")
    @PostConstruct
    private void changeValidPromotion(){
        List<PromotionCode> promotionCodes = promotionCodeRepository.findAll();
        for (PromotionCode item : promotionCodes) {
            if (item.getEndDate().isBefore(LocalDate.now()) || item.getStartDate().isAfter(LocalDate.now())) {
                item.setValid(false);
                promotionCodeRepository.save(item);
            }else{
                item.setValid(true);
                promotionCodeRepository.save(item);
            }
        }
        List<PromotionFixed> promotionFixeds = promotionFixedRepository.findAll();
        for (PromotionFixed item : promotionFixeds) {
            if (item.getEndDate().isBefore(LocalDate.now()) || item.getStartDate().isAfter(LocalDate.now())) {
                item.setValid(false);
                promotionFixedRepository.save(item);
            }else {
                item.setValid(true);
                promotionFixedRepository.save(item);
            }
        }
    }

    public ResponseEntity<GenericResponse> getPromotionFixeds() {
        try {
            List<PromotionFixed> promotionFixeds = promotionFixedRepository.findAllByIsDeletedIsFalseAndIsValidIsTrue();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Danh sách khuyến mãi.")
                            .result(promotionFixeds)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
