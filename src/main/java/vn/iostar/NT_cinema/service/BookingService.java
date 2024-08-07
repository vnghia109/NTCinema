package vn.iostar.NT_cinema.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.iostar.NT_cinema.constant.PriceType;
import vn.iostar.NT_cinema.constant.TicketStatus;
import vn.iostar.NT_cinema.util.PaginationUtils;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.*;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookingService {
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    SeatRepository seatRepository;
    @Autowired
    FoodRepository foodRepository;
    @Autowired
    JavaMailSender mailSender;
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    EmailVerificationRepository emailVerificationRepository;
    @Autowired
    Environment env;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ShowTimeRepository showTimeRepository;
    @Autowired
    ManagerRepository managerRepository;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    CinemaRepository cinemaRepository;
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    FoodInventoryRepository foodInventoryRepository;
    @Autowired
    DailyStatsRepository dailyStatsRepository;
    @Autowired
    MonthlyStatsRepository monthlyStatsRepository;
    @Autowired
    UserStatsRepository userStatsRepository;
    @Autowired
    CinemaFinanceStatsRepository cinemaFinanceStatsRepository;
    @Autowired
    StaffRepository staffRepository;
    @Autowired
    StaffStatsRepository staffStatsRepository;
    @Autowired
    PromotionService promotionService;
    @Autowired
    PromotionCodeRepository promotionCodeRepository;
    @Autowired
    PromotionFixedRepository promotionFixedRepository;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    NotificationService notificationService;

    public void handleBookingChange(Booking booking) {
        if(booking.isPayment() && !booking.getTicketStatus().equals(TicketStatus.CANCELLED)) {
            Optional<DailyStats> dailyStats = dailyStatsRepository.findByCinemaAndDate(booking.getSeats().get(0).getShowTime().getRoom().getCinema(), booking.getCreateAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            if (dailyStats.isPresent()) {
                dailyStats.get().setTotalOfTickets(dailyStats.get().getTotalOfTickets() + booking.getSeats().size());
                dailyStats.get().setTotalOfBookings(dailyStats.get().getTotalOfBookings() + 1);
                dailyStats.get().setRevenue(dailyStats.get().getRevenue().add(booking.getTotal()));
                dailyStatsRepository.save(dailyStats.get());
            }else {
                dailyStatsRepository.save(new DailyStats(booking.getCreateAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        booking.getSeats().get(0).getShowTime().getRoom().getCinema(),
                        booking.getTotal(), booking.getSeats().size(), 1));
            }

            LocalDate date = booking.getCreateAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1);
            Optional<MonthlyStats> monthlyStats = monthlyStatsRepository.findByCinemaAndMonth(booking.getSeats().get(0).getShowTime().getRoom().getCinema(), date);
            if (monthlyStats.isPresent()) {
                monthlyStats.get().setTotalOfTickets(monthlyStats.get().getTotalOfTickets() + booking.getSeats().size());
                monthlyStats.get().setTotalOfBookings(monthlyStats.get().getTotalOfBookings() + 1);
                monthlyStats.get().setRevenue(monthlyStats.get().getRevenue().add(booking.getTotal()));
                monthlyStatsRepository.save(monthlyStats.get());
            }else {
                monthlyStatsRepository.save(new MonthlyStats(booking.getCreateAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1),
                        booking.getSeats().get(0).getShowTime().getRoom().getCinema(),
                        booking.getTotal(), booking.getSeats().size(), 1));
            }

            Optional<User> user = userRepository.findById(booking.getUserId());
            if (user.isPresent()) {
                Optional<UserStats> userStats = userStatsRepository.findByUser_UserId(booking.getUserId());
                if (userStats.isPresent()) {
                    userStats.get().setTotalSpent(userStats.get().getTotalSpent().add(booking.getTotal()));
                    userStats.get().setTotalOfTickets(userStats.get().getTotalOfTickets() + booking.getSeats().size());
                    userStats.get().setTotalOfBookings(userStats.get().getTotalOfBookings() + 1);
                    userStatsRepository.save(userStats.get());
                }else {
                    userStatsRepository.save(new UserStats(user.get(), booking.getTotal(), 1, booking.getSeats().size()));
                }
            }

            Optional<CinemaFinanceStats> financeStats = cinemaFinanceStatsRepository.findByCinemaAndMonth(
                    booking.getSeats().get(0).getShowTime().getRoom().getCinema(),
                    booking.getCreateAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1)
            );
            if (financeStats.isPresent()) {
                financeStats.get().setTotalRevenue(financeStats.get().getTotalRevenue().add(booking.getSeatTotalPrice().add(booking.getFoodTotalPrice())));
                financeStats.get().setTicketRevenue(financeStats.get().getTicketRevenue().add(booking.getSeatTotalPrice()));
                financeStats.get().setFoodRevenue(financeStats.get().getFoodRevenue().add(booking.getFoodTotalPrice()));
                financeStats.get().setOtherExpense(financeStats.get().getOtherExpense().add(booking.getDiscount()));
                financeStats.get().setTotalExpense(financeStats.get().getTotalExpense().add(booking.getDiscount()));
                financeStats.get().setTotalOfBooking(financeStats.get().getTotalOfBooking() + 1);
                financeStats.get().calculateProfit();
                cinemaFinanceStatsRepository.save(financeStats.get());
            }else {
                CinemaFinanceStats cinemaFinanceStats = new CinemaFinanceStats(
                        booking.getCreateAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1),
                        booking.getSeats().get(0).getShowTime().getRoom().getCinema(),
                        booking.getSeatTotalPrice().add(booking.getFoodTotalPrice()),
                        booking.getSeatTotalPrice(),
                        booking.getFoodTotalPrice(),
                        1, booking.getDiscount(),
                        booking.getDiscount());
                cinemaFinanceStats.calculateProfit();
                cinemaFinanceStatsRepository.save(cinemaFinanceStats);
            }
        }
        if (booking.isPayment() && booking.getTicketStatus().equals(TicketStatus.CANCELLED)) {
            Optional<DailyStats> dailyStats = dailyStatsRepository.findByCinemaAndDate(booking.getSeats().get(0).getShowTime().getRoom().getCinema(), booking.getCreateAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            if (dailyStats.isPresent()) {
                dailyStats.get().setTotalOfTickets(dailyStats.get().getTotalOfTickets() - booking.getSeats().size());
                dailyStats.get().setTotalOfBookings(dailyStats.get().getTotalOfBookings() - 1);
                dailyStats.get().setRevenue(dailyStats.get().getRevenue().subtract(booking.getTotal()));
                dailyStatsRepository.save(dailyStats.get());
            }
            LocalDate date = booking.getCreateAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1);
            Optional<MonthlyStats> monthlyStats = monthlyStatsRepository.findByCinemaAndMonth(booking.getSeats().get(0).getShowTime().getRoom().getCinema(), date);
            if (monthlyStats.isPresent()) {
                monthlyStats.get().setTotalOfTickets(monthlyStats.get().getTotalOfTickets() - booking.getSeats().size());
                monthlyStats.get().setTotalOfBookings(monthlyStats.get().getTotalOfBookings() - 1);
                monthlyStats.get().setRevenue(monthlyStats.get().getRevenue().subtract(booking.getTotal()));
                monthlyStatsRepository.save(monthlyStats.get());
            }

            Optional<UserStats> userStats = userStatsRepository.findByUser_UserId(booking.getUserId());
            if (userStats.isPresent()) {
                userStats.get().setTotalSpent(userStats.get().getTotalSpent().subtract(booking.getTotal()));
                userStats.get().setTotalOfTickets(userStats.get().getTotalOfTickets() - booking.getSeats().size());
                userStats.get().setTotalOfBookings(userStats.get().getTotalOfBookings() - 1);
                userStatsRepository.save(userStats.get());
            }

            Optional<CinemaFinanceStats> financeStats = cinemaFinanceStatsRepository.findByCinemaAndMonth(
                    booking.getSeats().get(0).getShowTime().getRoom().getCinema(),
                    booking.getCreateAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1)
            );
            if (financeStats.isPresent()) {
                financeStats.get().setTotalRevenue(financeStats.get().getTotalRevenue().subtract(booking.getSeatTotalPrice().add(booking.getFoodTotalPrice())));
                financeStats.get().setTicketRevenue(financeStats.get().getTicketRevenue().subtract(booking.getSeatTotalPrice()));
                financeStats.get().setFoodRevenue(financeStats.get().getFoodRevenue().subtract(booking.getFoodTotalPrice()));
                financeStats.get().setOtherExpense(financeStats.get().getOtherExpense().subtract(booking.getDiscount()));
                financeStats.get().setTotalExpense(financeStats.get().getTotalExpense().subtract(booking.getDiscount()));
                financeStats.get().calculateProfit();
                cinemaFinanceStatsRepository.save(financeStats.get());
            }
        }
    }

    public ResponseEntity<GenericResponse> bookTicket(String bookingId) {
        try {
            Optional<Booking> booking = bookingRepository.findById(bookingId);
            if (booking.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Quý khách vui lòng chọn lại ghế và đồ ăn!")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            List<Seat> seats = booking.get().getSeats();
            for (Seat item: seats) {
                if (!item.isStatus()){
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(GenericResponse.builder()
                                    .success(false)
                                    .message("Ghế bạn muốn đặt đã bị người khác nhanh tay hơn đặt trước. Vui lòng chọn lại ghế khác!")
                                    .result(item)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .build());
                }
                item.setStatus(false);
                seatRepository.save(item);
            }
            booking.get().setTicketStatus(TicketStatus.UNCONFIRMED);
            bookingRepository.save(booking.get());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Đặt vé thành công. Vui lòng thanh toán!!")
                            .result(booking)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> bookingInfo(String userId, BookReq bookReq) {
        try {
            List<String> seatIds = bookReq.getSeatIds();
            List<String> foodIds = bookReq.getFoodIds();
            List<Seat> seats = new ArrayList<>();
            for (String item: seatIds) {
                Optional<Seat> seat = seatRepository.findById(item);
                if (seat.isPresent()){
                    if (!seat.get().isStatus()){
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(GenericResponse.builder()
                                        .success(false)
                                        .message("Ghế đã được đặt trước rồi!")
                                        .result(seat.get())
                                        .statusCode(HttpStatus.CONFLICT.value())
                                        .build());
                    }
                    seats.add(seat.get());
                }
            }
            List<FoodWithCount> foods = convertToFoodWithCountList(foodIds);
            Booking booking;
            Criteria criteria = Criteria.where("isPayment").is(false);
            criteria.and("userId").is(userId)
                    .and("showtimeId").is(seats.get(0).getShowTime().getShowTimeId());
            Query query = new Query(criteria);
            List<Booking> bookingList = mongoTemplate.find(query, Booking.class);
            if (!bookingList.isEmpty())
                booking = bookingList.get(0);
            else
                booking = new Booking();
            booking.setUserId(userId);
            booking.setShowtimeId(seats.get(0).getShowTime().getShowTimeId());
            booking.setCreateAt(new Date());
            booking.setSeats(seats);
            booking.setFoods(foods);
            booking.setCinemaId(seats.get(0).getShowTime().getRoom().getCinema().getCinemaId());
            booking.setDiscount(BigDecimal.ZERO);
            booking.setPromotionCode(null);
            Map<String, BigDecimal> seatPriceMap = new HashMap<>();
            List<PromotionFixed> promotionFixedList = totalBooking(booking, seatPriceMap);
            booking.setPromotionFixeds(promotionFixedList);
            List<SeatPromotion> seatPromotions = new ArrayList<>();
            for (Seat item: seats) {
                if (seatPriceMap.containsKey(item.getSeatId())){
                    seatPromotions.add(new SeatPromotion(item.getSeatId(), BigDecimal.valueOf(item.getPrice().getPrice()), seatPriceMap.get(item.getSeatId()), item.getRow(), item.getColumn()));
                }
            }
            Optional<PromotionCode> promotionCode;
            if (bookReq.getCode() != null && !bookReq.getCode().isEmpty()){
                promotionCode = promotionCodeRepository.findByPromotionCode(bookReq.getCode());
                if (promotionCode.isEmpty()){
                    Booking bookingRes = bookingRepository.save(booking);
                    BookingInfoRes bookingInfoRes = new BookingInfoRes(bookingRes, promotionFixedList, seatPromotions);
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(GenericResponse.builder()
                                    .success(true)
                                    .message("Mã khuyến mãi không tồn tại!")
                                    .result(bookingInfoRes)
                                    .statusCode(HttpStatus.OK.value())
                                    .build());
                }
                Map<Boolean, String> map = promotionService.checkPromotionCode(promotionCode.get(), booking);
                if (map.containsKey(false)){
                    Booking bookingRes = bookingRepository.save(booking);
                    BookingInfoRes bookingInfoRes = new BookingInfoRes(bookingRes, promotionFixedList, seatPromotions);
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(GenericResponse.builder()
                                    .success(true)
                                    .message(map.get(false))
                                    .result(bookingInfoRes)
                                    .statusCode(HttpStatus.OK.value())
                                    .build());
                }else {
                    BigDecimal totalAfDiscount = promotionService.calculateTotal(booking, promotionCode.get());
                    booking.setDiscount(booking.getTotal().subtract(totalAfDiscount));
                    booking.setTotal(totalAfDiscount);
                    booking.setPromotionCode(promotionCode.get());
                }
            }
            Booking bookingRes = bookingRepository.save(booking);
            BookingInfoRes bookingInfoRes = new BookingInfoRes(bookingRes, promotionFixedList, seatPromotions);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy thông tin đặt lịch thành công!")
                            .result(bookingInfoRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public void sendEmailBookingSuccess(Booking booking) {
        try {
            Optional<User> user = userRepository.findById(booking.getUserId());
            if (user.isEmpty()){
                return;
            }
            Optional<ShowTime> showTime = showTimeRepository.findById(booking.getSeats().get(0).getShowTime().getShowTimeId());
            if (showTime.isEmpty()){
                return;
            }
            Schedule schedule = booking.getSeats().get(0).getSchedule();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(user.get().getEmail());

            // Load Thymeleaf template
            Context context = new Context();
            context.setVariable("bookingCode", booking.getBookingId());
            context.setVariable("movieName", showTime.get().getMovie().getTitle());
            context.setVariable("date", schedule.getDate());
            context.setVariable("startTime", schedule.getStartTime());
            context.setVariable("ticketCount", booking.getSeats().size());
            context.setVariable("foods", booking.getFoods());
            context.setVariable("seats", booking.getSeats());
            context.setVariable("totalAmount", booking.getTotal());
            String mailContent = templateEngine.process("booking-success", context);

            helper.setText(mailContent, true);
            helper.setSubject("Đặt vé xem phim thành công TNCinemas");
            helper.setFrom(Objects.requireNonNull(env.getProperty("spring.mail.username")),"TNCinemas Admin");
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<FoodWithCount> convertToFoodWithCountList(List<String> foods) {
        Map<String, Integer> foodCountMap = new HashMap<>();

        for (String food : foods) {
            foodCountMap.put(food, foodCountMap.getOrDefault(food, 0) + 1);
        }

        List<FoodWithCount> foodWithCounts = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : foodCountMap.entrySet()) {
            FoodWithCount foodWithCount = new FoodWithCount(foodRepository.findById(entry.getKey()).orElse(null), entry.getValue());
            foodWithCounts.add(foodWithCount);
        }

        return foodWithCounts;
    }

    public List<PromotionFixed> totalBooking(Booking booking, Map<String, BigDecimal> map) {
        BigDecimal total, seatTotalPrice = BigDecimal.ZERO, foodTotalPrice = BigDecimal.ZERO;
        BigDecimal seatPrice = BigDecimal.ZERO;
        List<PromotionFixed> promotionFixedList = promotionService.listPromotionFixedAvailable(booking);
        for (Seat item : booking.getSeats()) {
            seatPrice = BigDecimal.valueOf(item.getPrice().getPrice());
            for (PromotionFixed promotion : promotionFixedList) {
                if (item.getPrice().getType().equals(PriceType.COUPLE) && seatPrice.compareTo(promotion.getCoupleValue()) > 0){
                    seatPrice = promotion.getCoupleValue();
                } else if (item.getPrice().getType().equals(PriceType.VIP) && seatPrice.compareTo(promotion.getVipValue()) > 0){
                    seatPrice = promotion.getVipValue();
                } else if (item.getPrice().getType().equals(PriceType.NORMAL) && seatPrice.compareTo(promotion.getNormalValue()) > 0){
                    seatPrice = promotion.getNormalValue();
                }
            }
            map.put(item.getSeatId(), seatPrice);
            seatTotalPrice = seatTotalPrice.add(seatPrice);
        }
        for (FoodWithCount item : booking.getFoods()) {
            foodTotalPrice = foodTotalPrice.add(BigDecimal.valueOf((long) item.getFood().getPrice() * item.getCount()));
        }
        total = seatTotalPrice.add(foodTotalPrice);
        booking.setSeatTotalPrice(seatTotalPrice);
        booking.setFoodTotalPrice(foodTotalPrice);
        booking.setTotal(total);
        return promotionFixedList;
    }

    public void deleteBookingNotPay(){
        Date now = new Date();
        List<Booking> bookings = bookingRepository.findAllByIsPaymentIsFalse();
        for (Booking item : bookings) {
            long diffInMinutes = (now.getTime() - item.getCreateAt().getTime()) / (60 * 1000);
            if (diffInMinutes > 10){
                setSeatStatusTrue(item.getSeats());
                bookingRepository.delete(item);
            }
        }
    }

    public  void setSeatStatusTrue(List<Seat> seats){
        for (Seat item : seats) {
            Optional<Seat> seat = seatRepository.findById(item.getSeatId());
            if (seat.isPresent()){
                seat.get().setStatus(true);
                seatRepository.save(seat.get());
            }
        }
    }

    @Scheduled(fixedDelay = 6000) //1 minutes
    public void cleanupBooking(){ deleteBookingNotPay(); }

    public ResponseEntity<?> getTicketDetail(String bookingId) {
        try {
            Optional<Booking> booking = bookingRepository.findById(bookingId);
            if (booking.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Không tìm thấy vé đã đặt.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            Optional<User> user = userRepository.findById(booking.get().getUserId());

            ShowTime showTime = booking.get().getSeats().get(0).getShowTime();
            Schedule schedule = booking.get().getSeats().get(0).getSchedule();
            TicketDetailRes ticket = new TicketDetailRes();
            ticket.setBookingId(booking.get().getBookingId());
            ticket.setMovieId(showTime.getMovie().getMovieId());
            ticket.setMovieName(showTime.getMovie().getTitle());
            if (user.isPresent()) {
                ticket.setUserName(user.get().getUserName());
                ticket.setFullName(user.get().getFullName());
            }else {
                ticket.setUserName(null);
                ticket.setFullName(null);
            }
            ticket.setDate(schedule.getDate());
            ticket.setStartTime(schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            ticket.setEndTime(schedule.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            ticket.setCinemaName(showTime.getRoom().getCinema().getCinemaName());
            ticket.setDuration(Integer.parseInt(showTime.getMovie().getDuration()));
            ticket.setRoomName(showTime.getRoom().getRoomName());
            List<SeatBookedRes> seats = new ArrayList<>();
            for (Seat seat : booking.get().getSeats()) {
                seats.add(new SeatBookedRes(seat.getRow(), seat.getColumn()));
            }
            ticket.setSeats(seats);
            ticket.setFoods(booking.get().getFoods().stream().map(FoodWithCount::getFood).map(Food::getName).collect(Collectors.toList()));
            ticket.setPrice(booking.get().getTotal());
            ticket.setPromotionCode(booking.get().getPromotionCode());
            ticket.setPromotionFixeds(booking.get().getPromotionFixeds());
            ticket.setStatus(booking.get().getTicketStatus());
            ticket.setCreateAt(booking.get().getCreateAt());
            ticket.setCancelTime(booking.get().getCancelTime());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy chi tiết vé thành công!")
                            .result(ticket)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
    public ResponseEntity<GenericResponse> getBookings(String status, String cinemaId, Pageable pageable) {
        try {
            List<Booking> bookings;
            Criteria criteria = Criteria.where("isPayment").is(true);
            if (cinemaId != null && !cinemaId.isEmpty()) {
                criteria.and("cinemaId").is(cinemaId);
            }
            if (status != null && !status.isEmpty()) {
                criteria.and("ticketStatus").is(TicketStatus.valueOf(status));
            }
            Query query = new Query(criteria);
            bookings = mongoTemplate.find(query, Booking.class);

            Page<Booking> result = PaginationUtils.paginate(bookings.stream().sorted(Comparator.comparing(Booking::getCreateAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed()).toList(), pageable);
            List<String> userIds = result.getContent().stream()
                    .map(Booking::getUserId)
                    .distinct() // Lọc ra các userId duy nhất
                    .collect(Collectors.toList());

            Map<String, User> userMap = userRepository.findAllById(userIds).stream()
                    .collect(Collectors.toMap(User::getUserId, Function.identity()));

            List<BookingsOfStaffRes> list = result.getContent().stream()
                    .map(item -> new BookingsOfStaffRes(item, userMap.get(item.getUserId())))
                    .toList();

            Map<String, Object> map = new HashMap<>();
            map.put("content", list);
            map.put("pageNumber", result.getPageable().getPageNumber() + 1);
            map.put("pageSize", result.getSize());
            map.put("totalPages", result.getTotalPages());
            map.put("totalElements", result.getTotalElements());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy tất cả vé đã đặt thành công.")
                            .result(map)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> confirmBooking(String bookingId) {
        try {
            Optional<Booking> booking = bookingRepository.findById(bookingId);
            if (booking.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Không tìm thấy vé đặt.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            booking.get().setTicketStatus(TicketStatus.CONFIRMED);
            bookingRepository.save(booking.get());
            notificationService.ticketStatusNotification(booking.get());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Đã xác nhận vé.")
                            .result(booking)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<?> cancelTicket(String bookingId, String userId) {
        try {
            Optional<Booking> booking = bookingRepository.findByBookingIdAndUserId(bookingId, userId);
            if (booking.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Vé muốn xóa không tồn tại.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            Schedule schedule = booking.get().getSeats().get(0).getSchedule();
            LocalDateTime localDateTime = LocalDateTime.of(schedule.getDate(), schedule.getStartTime());
            Date start = Date.from(localDateTime.minusHours(1).atZone(ZoneId.systemDefault()).toInstant());
            if (start.before(new Date())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Vé đã quá thời hạn để hủy. Vé chỉ có thể hủy trước giờ chiếu 1 tiếng.")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .build());
            }
            booking.get().setTicketStatus(TicketStatus.CANCELLED);
            booking.get().setCancelTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            bookingRepository.save(booking.get());
            handleBookingChange(booking.get());
            notificationService.ticketStatusNotification(booking.get());

            for (Seat seat: booking.get().getSeats()) {
                seat.setStatus(true);
                seatRepository.save(seat);
            }

            for (FoodWithCount item: booking.get().getFoods()) {
                Food food = item.getFood();
                food.setQuantity(food.getQuantity() + item.getCount());
                foodRepository.save(food);
                Cinema cinema = booking.get().getSeats().get(0).getShowTime().getRoom().getCinema();
                Optional<FoodInventory> foodInventory = foodInventoryRepository.findByFoodAndCinema(food, cinema);
                foodInventory.ifPresent(inventory -> inventory.setQuantity(inventory.getQuantity() + item.getCount()));
                foodInventory.ifPresent(inventory -> foodInventoryRepository.save(inventory));
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Đã hủy vé thành công!")
                            .result(booking)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<?> getTicketCanceled(String userId) {
        try {
            List<Booking> bookings = bookingRepository.findAllByUserIdAndTicketStatus(userId, TicketStatus.CANCELLED);
            List<HistoryMovieRes> historyMovieRes = new ArrayList<>();
            for (Booking item : bookings) {
                Movie movie = item.getSeats().get(0).getShowTime().getMovie();
                historyMovieRes.add(new HistoryMovieRes(item.getBookingId(),
                        movie.getMovieId(),
                        movie.getTitle(),
                        item.getSeats().get(0).getShowTime().getRoom().getCinema().getCinemaName(),
                        item.getSeats().get(0).getSchedule().getDate(),
                        item.getSeats().get(0).getSchedule().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        item.getTotal(),
                        item.getCreateAt(),
                        item.getTicketStatus()));
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy danh sách vé đã hủy thành công!")
                            .result(historyMovieRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> sellTicket(String staffId, SellTicketReq request) {
        try {
            Optional<Staff> staff = staffRepository.findById(staffId);
            if (staff.isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Không tìm thấy nhân viên. Hãy đăng nhập lại!")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .build());
            }

            List<String> seatIds = request.getSeatIds();
            List<String> foodIds = request.getFoodIds();
            List<Seat> seats = new ArrayList<>();
            for (String item: seatIds) {
                Optional<Seat> seat = seatRepository.findById(item);
                if (seat.isPresent()){
                    if (!seat.get().isStatus()){
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(GenericResponse.builder()
                                        .success(false)
                                        .message("Ghế đã được đặt trước rồi!")
                                        .result(seat.get())
                                        .statusCode(HttpStatus.CONFLICT.value())
                                        .build());
                    }
                    seat.get().setStatus(false);
                    seatRepository.save(seat.get());
                    seats.add(seat.get());
                }
            }
            if (seats.isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Vui lòng chọn ghế muốn mua!")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .build());
            }
            List<FoodWithCount> foods = convertToFoodWithCountList(foodIds);

            Booking booking = new Booking();
            booking.setUserId(request.getUserId());
            booking.setShowtimeId(seats.get(0).getShowTime().getShowTimeId());
            booking.setCreateAt(new Date());
            booking.setSeats(seats);
            booking.setFoods(foods);
            booking.setCinemaId(seats.get(0).getShowTime().getRoom().getCinema().getCinemaId());
            booking.setDiscount(BigDecimal.ZERO);
            booking.setPromotionCode(null);
            Map<String, BigDecimal> seatPriceMap = new HashMap<>();
            List<PromotionFixed> promotionFixedList = totalBooking(booking, seatPriceMap);
            booking.setPromotionFixeds(promotionFixedList);
            List<SeatPromotion> seatPromotions = new ArrayList<>();
            for (Seat item: seats) {
                if (seatPriceMap.containsKey(item.getSeatId())){
                    seatPromotions.add(new SeatPromotion(item.getSeatId(), BigDecimal.valueOf(item.getPrice().getPrice()), seatPriceMap.get(item.getSeatId()), item.getRow(), item.getColumn()));
                }
            }
            booking.setPayment(true);
            booking.setTicketStatus(TicketStatus.CONFIRMED);
            if (request.getCode() != null && !request.getCode().isEmpty()){
                Optional<PromotionCode> promotionCode = promotionCodeRepository.findByPromotionCode(request.getCode());
                if (promotionCode.isEmpty()){
                    Booking bookingRes = bookingRepository.save(booking);
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(GenericResponse.builder()
                                    .success(true)
                                    .message("Mã khuyến mãi không tồn tại!")
                                    .result(bookingRes)
                                    .statusCode(HttpStatus.OK.value())
                                    .build());
                }
                Map<Boolean, String> map = promotionService.checkPromotionCode(promotionCode.get(), booking);
                if (map.containsKey(false)){
                    Booking bookingRes = bookingRepository.save(booking);
                    BookingInfoRes bookingInfoRes = new BookingInfoRes(bookingRes, promotionFixedList, seatPromotions);
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(GenericResponse.builder()
                                    .success(true)
                                    .message(map.get(false))
                                    .result(bookingInfoRes)
                                    .statusCode(HttpStatus.OK.value())
                                    .build());
                }else {
                    BigDecimal totalAfDiscount = promotionService.calculateTotal(booking, promotionCode.get());
                    booking.setDiscount(booking.getTotal().subtract(totalAfDiscount));
                    booking.setTotal(totalAfDiscount);
                    booking.setPromotionCode(promotionCode.get());
                }
            }
            Booking bookingRes = bookingRepository.save(booking);

            handleBookingChange(bookingRes);
            Optional<StaffStats> staffStats = staffStatsRepository.findByStaff(staff.get());
            if (staffStats.isPresent()) {
                staffStats.get().setRevenue(staffStats.get().getRevenue().add(booking.getTotal()));
                staffStats.get().setTotalOfTickets(staffStats.get().getTotalOfTickets() + booking.getSeats().size());
                staffStatsRepository.save(staffStats.get());
            }else {
                staffStatsRepository.save(new StaffStats(staff.get(), booking.getTotal(), 1));
            }

            if (bookingRes.getUserId() != null) {
                sendEmailBookingSuccess(bookingRes);
            }
            notificationService.sellTicketSuccessNotification(bookingRes, staff.get());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Đặt vé thành công!!")
                            .result(bookingRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> sellTicketInfo(SellTicketReq req) {
        try {
            List<String> seatIds = req.getSeatIds();
            List<String> foodIds = req.getFoodIds();
            List<Seat> seats = new ArrayList<>();
            for (String item: seatIds) {
                Optional<Seat> seat = seatRepository.findById(item);
                if (seat.isPresent()){
                    if (!seat.get().isStatus()){
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(GenericResponse.builder()
                                        .success(false)
                                        .message("Ghế đã được đặt trước rồi!")
                                        .result(seat.get())
                                        .statusCode(HttpStatus.CONFLICT.value())
                                        .build());
                    }
                    seats.add(seat.get());
                }
            }
            List<FoodWithCount> foods = convertToFoodWithCountList(foodIds);
            Booking booking = new Booking();
            booking.setUserId(req.getUserId());
            booking.setShowtimeId(seats.get(0).getShowTime().getShowTimeId());
            booking.setCreateAt(new Date());
            booking.setSeats(seats);
            booking.setFoods(foods);
            booking.setDiscount(BigDecimal.ZERO);
            booking.setPromotionCode(null);
            Map<String, BigDecimal> seatPriceMap = new HashMap<>();
            List<PromotionFixed> promotionFixedList = totalBooking(booking, seatPriceMap);
            booking.setPromotionFixeds(promotionFixedList);
            List<SeatPromotion> seatPromotions = new ArrayList<>();
            for (Seat item: seats) {
                if (seatPriceMap.containsKey(item.getSeatId())){
                    seatPromotions.add(new SeatPromotion(item.getSeatId(), BigDecimal.valueOf(item.getPrice().getPrice()), seatPriceMap.get(item.getSeatId()), item.getRow(), item.getColumn()));
                }
            }
            Optional<PromotionCode> promotionCode;
            if (req.getCode() != null && !req.getCode().isEmpty()){
                promotionCode = promotionCodeRepository.findByPromotionCode(req.getCode());
                if (promotionCode.isEmpty()){
                    BookingInfoRes bookingInfoRes = new BookingInfoRes(booking, promotionFixedList, seatPromotions);
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(GenericResponse.builder()
                                    .success(true)
                                    .message("Mã khuyến mãi không tồn tại!")
                                    .result(bookingInfoRes)
                                    .statusCode(HttpStatus.OK.value())
                                    .build());
                }
                Map<Boolean, String> map = promotionService.checkPromotionCode(promotionCode.get(), booking);
                if (map.containsKey(false)){
                    BookingInfoRes bookingInfoRes = new BookingInfoRes(booking, promotionFixedList, seatPromotions);
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(GenericResponse.builder()
                                    .success(true)
                                    .message(map.get(false))
                                    .result(bookingInfoRes)
                                    .statusCode(HttpStatus.OK.value())
                                    .build());
                }else {
                    BigDecimal totalAfDiscount = promotionService.calculateTotal(booking, promotionCode.get());
                    booking.setDiscount(booking.getTotal().subtract(totalAfDiscount));
                    booking.setTotal(totalAfDiscount);
                    booking.setPromotionCode(promotionCode.get());
                }
            }
            BookingInfoRes bookingInfoRes = new BookingInfoRes(booking, promotionFixedList, seatPromotions);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy thông tin đặt lịch thành công!")
                            .result(bookingInfoRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Booking> findBySeat(Seat seat){
        Criteria criteria = Criteria.where("seats").elemMatch(Criteria.where("seatId").is(seat.getSeatId()));
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Booking.class);
    }
}
