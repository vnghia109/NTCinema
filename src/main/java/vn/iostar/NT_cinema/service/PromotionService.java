package vn.iostar.NT_cinema.service;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;
import de.jollyday.parameter.CalendarPartManagerParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.DiscountType;
import vn.iostar.NT_cinema.constant.PromotionType;
import vn.iostar.NT_cinema.dto.ApplyPromotion;
import vn.iostar.NT_cinema.dto.FoodWithCount;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.PromotionReq;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.PromotionRepository;
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
    PromotionRepository promotionRepository;
    @Autowired
    UserRepository userRepository;

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
            promotion.setPromotionType(PromotionType.valueOf(promotionReq.getPromotionType()));
            promotion.setPromotionCode(promotionReq.getPromotionCode());
            promotion.setMaxUsage(promotionReq.getMaxUsage());
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
                promotion.get().setPromotionType(PromotionType.valueOf(promotionReq.getPromotionType()));
                promotion.get().setPromotionCode(promotionReq.getPromotionCode());
                promotion.get().setMaxUsage(promotionReq.getMaxUsage());
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

    public ApplyPromotion applyApplicablePromotions(Booking booking) {
        BigDecimal totalDiscount = BigDecimal.ZERO;
        List<Promotion> promotions = promotionRepository.findAll();
        List<Promotion> applyPromotions = new ArrayList<>();

        for (Promotion promotion : promotions) {
            if (promotion.getDiscountType() != DiscountType.SPECIAL_OFFER && promotion.getPromotionType() == PromotionType.FIXED) {
                if (isPromotionApplicableFixed(promotion, booking)) {
                    BigDecimal discount = calculateDiscount(promotion, booking);
                    totalDiscount = totalDiscount.add(discount);
                    applyPromotions.add(promotion);
                }
            }
            if (promotion.getPromotionType() == PromotionType.CODE) {

            }
        }

        return new ApplyPromotion(applyPromotions, totalDiscount);
    }

    private  boolean isPromotionApplicableCode(Promotion promotion, Booking booking) {
        if (promotion.isDeleted()) {
            return false;
        }

        if (promotion.getMaxUsage() != null && promotion.getMaxUsage() <= 0) {
            return false;
        }

        return true;
    }

    private boolean isPromotionApplicableFixed(Promotion promotion, Booking booking) {
        // Check if the promotion is not deleted
        if (promotion.isDeleted()) {
            return false;
        }

        // Check if we are within the promotion period
        LocalDateTime localDateTime = LocalDateTime.of(booking.getSeats().get(0).getSchedule().getDate(), booking.getSeats().get(0).getSchedule().getStartTime());
        Date currentDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        if (currentDate.before(promotion.getStartDate()) || currentDate.after(promotion.getEndDate())) {
            return false;
        }

        // Check if the promotion is applicable for the current day of week
        if (promotion.getValidDayOfWeek() != null && promotion.getValidDayOfWeek() != booking.getSeats().get(0).getSchedule().getDate().getDayOfWeek().getValue()) {
            return false;
        }

        LocalDate currentDateLocalDate = booking.getSeats().get(0).getSchedule().getDate();
        if (promotion.isExcludeHolidays() && isPublicHoliday(currentDateLocalDate, "vn")){
            return false;
        }

        if (promotion.getAgeLimit() != null && promotion.getAgeLimit() > getUserAge(booking.getUserId())) {
            return false;
        }

        LocalTime localTime = booking.getSeats().get(0).getSchedule().getStartTime();
        if (promotion.getValidTimeFrameStart() != null && promotion.getValidTimeFrameEnd() != null) {
            return !localTime.isBefore(promotion.getValidTimeFrameStart()) && !localTime.isAfter(promotion.getValidTimeFrameEnd());
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

    private BigDecimal calculateDiscount(Promotion promotion, Booking order) {
        BigDecimal discount = BigDecimal.ZERO;

        switch (promotion.getDiscountType()) {
            case PERCENTAGE:
                List<Seat> seats = order.getSeats();
                for (Seat item : seats) {
                    int total = item.getPrice().getPrice();
                    discount = discount.add(BigDecimal.valueOf(total).multiply(promotion.getDiscountValue().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)));
                }
                break;
            case FIXED_AMOUNT:
                discount = promotion.getDiscountValue().multiply(BigDecimal.valueOf(order.getSeats().size()));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + promotion.getDiscountType());
        }

        return discount;
    }
}
