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
import vn.iostar.NT_cinema.dto.PromotionReq;
import vn.iostar.NT_cinema.entity.*;
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

    public ResponseEntity<GenericResponse> createPromotion(PromotionReq promotionReq) {
        try {
            PromotionFixed promotionFixed = new PromotionFixed();
            promotionFixed.setName(promotionReq.getName());
            promotionFixed.setDescription(promotionReq.getDescription());
            promotionFixed.setDiscountType(DiscountType.valueOf(promotionReq.getDiscountType()));
            promotionFixed.setDiscountValue(promotionReq.getDiscountValue());
            promotionFixed.setValidDayOfWeek(promotionReq.getValidDayOfWeek());
            promotionFixed.setAgeLimit(promotionReq.getAgeLimit());
            promotionFixed.setValidTimeFrameStart(promotionReq.getValidTimeFrameStart());
            promotionFixed.setValidTimeFrameEnd(promotionReq.getValidTimeFrameEnd());
            promotionFixed.setExcludeHolidays(promotionReq.isExcludeHolidays());
            promotionFixed.setStartDate(promotionReq.getStartDate());
            promotionFixed.setEndDate(promotionReq.getEndDate());

            promotionFixedRepository.save(promotionFixed);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Một khuyến mãi đã được thêm!")
                            .result(promotionFixed)
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
            Optional<PromotionFixed> promotion = promotionFixedRepository.findById(id);
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

    public ResponseEntity<GenericResponse> deletePromotion(String id) {
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

    public ApplyPromotion applyApplicablePromotions(Booking booking) {
        BigDecimal totalDiscount = BigDecimal.ZERO;
        List<PromotionFixed> promotionFixes = promotionFixedRepository.findAll();
        List<PromotionFixed> applyPromotionFixeds = new ArrayList<>();

        for (PromotionFixed promotionFixed : promotionFixes) {
            if (promotionFixed.getDiscountType() != DiscountType.SPECIAL_OFFER) {
                if (isPromotionApplicableFixed(promotionFixed, booking)) {
                    BigDecimal discount = calculateDiscount(promotionFixed, booking);
                    totalDiscount = totalDiscount.add(discount);
                    applyPromotionFixeds.add(promotionFixed);
                }
            }
        }

        return new ApplyPromotion(applyPromotionFixeds, totalDiscount);
    }

    private  boolean isPromotionApplicableCode(PromotionCode promotionCode, Booking booking) {
        if (promotionCode.isDeleted()) {
            return false;
        }

        if (promotionCode.getMaxUsage() != null && promotionCode.getMaxUsage() <= 0) {
            return false;
        }

        return true;
    }

    private boolean isPromotionApplicableFixed(PromotionFixed promotionFixed, Booking booking) {
        // Check if the promotion is not deleted
        if (promotionFixed.isDeleted()) {
            return false;
        }

        // Check if we are within the promotion period
        LocalDateTime localDateTime = LocalDateTime.of(booking.getSeats().get(0).getSchedule().getDate(), booking.getSeats().get(0).getSchedule().getStartTime());
        Date currentDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        if (currentDate.before(promotionFixed.getStartDate()) || currentDate.after(promotionFixed.getEndDate())) {
            return false;
        }

        // Check if the promotion is applicable for the current day of week
        if (promotionFixed.getValidDayOfWeek() != null && promotionFixed.getValidDayOfWeek() != booking.getSeats().get(0).getSchedule().getDate().getDayOfWeek().getValue()) {
            return false;
        }

        LocalDate currentDateLocalDate = booking.getSeats().get(0).getSchedule().getDate();
        if (promotionFixed.isExcludeHolidays() && isPublicHoliday(currentDateLocalDate, "vn")){
            return false;
        }

        if (promotionFixed.getAgeLimit() != null && promotionFixed.getAgeLimit() > getUserAge(booking.getUserId())) {
            return false;
        }

        LocalTime localTime = booking.getSeats().get(0).getSchedule().getStartTime();
        if (promotionFixed.getValidTimeFrameStart() != null && promotionFixed.getValidTimeFrameEnd() != null) {
            return !localTime.isBefore(promotionFixed.getValidTimeFrameStart()) && !localTime.isAfter(promotionFixed.getValidTimeFrameEnd());
        }

        return true;
    }

    public int getUserAge(String userId) {
        User user = userRepository.findById(userId).get();
        LocalDate date = user.getDob().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return LocalDate.now().getYear() - date.getYear();
    }

    public boolean isPublicHoliday(LocalDate date, String countryCode) {
        Properties properties = new Properties();
        CalendarPartManagerParameter param = new CalendarPartManagerParameter(countryCode, properties);
        HolidayManager manager = HolidayManager.getInstance(param);
        Set<LocalDate> holidays = manager.getHolidays(date.getYear()).stream()
                .map(Holiday::getDate)
                .collect(Collectors.toSet());
        return holidays.contains(date);
    }

    private BigDecimal calculateDiscount(PromotionFixed promotionFixed, Booking order) {
        BigDecimal discount = BigDecimal.ZERO;

        switch (promotionFixed.getDiscountType()) {
            case PERCENTAGE:
                List<Seat> seats = order.getSeats();
                for (Seat item : seats) {
                    int total = item.getPrice().getPrice();
                    discount = discount.add(BigDecimal.valueOf(total).multiply(promotionFixed.getDiscountValue().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)));
                }
                break;
            case FIXED_AMOUNT:
                discount = promotionFixed.getDiscountValue().multiply(BigDecimal.valueOf(order.getSeats().size()));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + promotionFixed.getDiscountType());
        }

        return discount;
    }
}
