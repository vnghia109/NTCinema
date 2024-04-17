package vn.iostar.NT_cinema.service;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.iostar.NT_cinema.constant.TicketStatus;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.*;
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

    public ResponseEntity<GenericResponse> bookTicket(String userId, BookReq bookReq) {
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
                    seat.get().setStatus(false);
                    seatRepository.save(seat.get());
                    seats.add(seat.get());
                }
            }
            List<Food> foods = new ArrayList<>();
            for (String item : foodIds) {
                Optional<Food> food = foodRepository.findById(item);
                food.ifPresent(foods::add);
            }
            Booking booking = new Booking();
            booking.setUserId(userId);
            booking.setShowtimeId(seats.get(0).getShowTime().getShowTimeId());
            booking.setCreateAt(new Date());
            booking.setSeats(seats);
            booking.setFoods(convertToFoodWithCountList(foodIds));
            booking.setTotal(totalBooking(seats, foods));
            booking.setTicketStatus(TicketStatus.UNCONFIRMED);

            Booking bookingRes = bookingRepository.save(booking);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Xác nhận thông tin thành công")
                            .result(bookingRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> bookingInfo(BookReq bookReq) {
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
            List<Food> foods = new ArrayList<>();
            for (String item : foodIds) {
                Optional<Food> food = foodRepository.findById(item);
                food.ifPresent(foods::add);
            }
            BookingInfoRes booking = new BookingInfoRes();
            booking.setCreateAt(new Date());
            booking.setSeats(seats);
            booking.setFoods(convertToFoodWithCountList(foodIds));
            booking.setTotal(totalBooking(seats, foods));

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy thông tin đặt lịch thành công!")
                            .result(booking)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public void sendEmailBookingSuccess(Booking booking) {
        try {
            Optional<User> user = userRepository.findById(booking.getUserId());
            ShowTime showTime = showTimeRepository.findById(booking.getSeats().get(0).getShowTime().getShowTimeId()).get();
            Schedule schedule = booking.getSeats().get(0).getSchedule();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(user.get().getEmail());

            // Load Thymeleaf template
            Context context = new Context();
            context.setVariable("bookingCode", booking.getBookingId());
            context.setVariable("movieName", showTime.getMovie().getTitle());
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
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<FoodWithCount> convertToFoodWithCountList(List<String> foods) {
        Map<String, Integer> foodCountMap = new HashMap<>();

        for (String food : foods) {
            foodCountMap.put(food, foodCountMap.getOrDefault(food, 0) + 1);
        }

        List<FoodWithCount> foodWithCounts = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : foodCountMap.entrySet()) {
            FoodWithCount foodWithCount = new FoodWithCount(foodRepository.findById(entry.getKey()).get(), entry.getValue());
            foodWithCounts.add(foodWithCount);
        }

        return foodWithCounts;
    }

    public int totalBooking(List<Seat> seats, List<Food> foods){
        int total = 0;
        for (Seat item : seats) {
            total += item.getPrice().getPrice();
        }
        for (Food item : foods) {
            total += item.getPrice();
        }
        return total;
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
            if (user.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Người dùng không tông tại.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            Optional<ShowTime> showTime = showTimeRepository.findById(booking.get().getSeats().get(0).getShowTime().getShowTimeId());
            Schedule schedule = booking.get().getSeats().get(0).getSchedule();
            TicketDetailRes ticket = new TicketDetailRes();
            ticket.setMovieId(showTime.get().getMovie().getMovieId());
            ticket.setMovieName(showTime.get().getMovie().getTitle());
            ticket.setUserName(user.get().getUserName());
            ticket.setFullName(user.get().getFullName());
            ticket.setDate(schedule.getDate());
            ticket.setStartTime(schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            ticket.setEndTime(schedule.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            ticket.setCinemaName(showTime.get().getRoom().getCinema().getCinemaName());
            ticket.setDuration(Integer.parseInt(showTime.get().getMovie().getDuration()));
            ticket.setRoomName(showTime.get().getRoom().getRoomName());
            List<SeatBookedRes> seats = new ArrayList<>();
            for (Seat seat : booking.get().getSeats()) {
                seats.add(new SeatBookedRes(seat.getRow(), seat.getColumn()));
            }
            ticket.setSeats(seats);
            ticket.setFoods(booking.get().getFoods().stream().map(FoodWithCount::getFood).map(Food::getName).collect(Collectors.toList()));
            ticket.setPrice(booking.get().getTotal());
            ticket.setStatus(booking.get().getTicketStatus());
            ticket.setCreateAt(booking.get().getCreateAt());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy chi tiết vé thành công!")
                            .result(ticket)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public int calculateTotalRevenue(List<Booking> paidBookings) {
        int totalRevenue = 0;

        for (Booking booking : paidBookings) {
            totalRevenue += booking.getTotal();
        }

        return totalRevenue;
    }

    public ResponseEntity<?> getBookingsInDateRange(Date startDate, Date endDate) {
        try {
            int total = calculateTotalRevenue(bookingRepository.findAllPaidBookingsInDateRange(startDate, endDate));

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy tổng doanh thu thành công!")
                            .result(total)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> getBookings(String status, String cinemaId, Pageable pageable) {
        try {
            Page<Booking> bookings;
            List<Room> rooms = roomRepository.findAllByCinema_CinemaId(cinemaId);
            if (cinemaId == null) {
                if (status.isEmpty() || status.isBlank()) {
                    bookings = bookingRepository.findAll(pageable);
                } else {
                    TicketStatus ticketStatus = TicketStatus.valueOf(status);
                    bookings = bookingRepository.findAllByTicketStatus(ticketStatus, pageable);
                }
            } else {
                List<ShowTime> showTimes = showTimeRepository.findAllByRoomIn(rooms);
                List<String> showtimeIds = showTimes.stream().map(ShowTime::getShowTimeId).toList();
                if (status.isEmpty() || status.isBlank()) {
                    bookings = bookingRepository.findAllByShowtimeIdIn(showtimeIds, pageable);
                } else {
                    TicketStatus ticketStatus = TicketStatus.valueOf(status);
                    bookings = bookingRepository.findAllByShowtimeIdInAndTicketStatus(showtimeIds, ticketStatus, pageable);
                }
            }

            List<BookingsOfStaffRes> list = new ArrayList<>();
            for (Booking item : bookings.getContent()) {
                Optional<User> user = userRepository.findById(item.getUserId());
                BookingsOfStaffRes bookingRes = new BookingsOfStaffRes();
                bookingRes.setBookingId(item.getBookingId());
                bookingRes.setUserName(user.get().getUserName());
                bookingRes.setFullName(user.get().getFullName());
                bookingRes.setCinemaName(item.getSeats().get(0).getShowTime().getRoom().getCinema().getCinemaName());
                bookingRes.setMovieName(item.getSeats().get(0).getShowTime().getMovie().getTitle());
                bookingRes.setMovieId(item.getSeats().get(0).getShowTime().getMovie().getMovieId());
                bookingRes.setDate(item.getSeats().get(0).getSchedule().getDate());
                bookingRes.setStartTime(item.getSeats().get(0).getSchedule().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                bookingRes.setPrice(item.getTotal());
                bookingRes.setCreateAt(item.getCreateAt());

                list.add(bookingRes);
            }

            Map<String, Object> map = new HashMap<>();
            map.put("content", list);
            map.put("pageNumber", bookings.getPageable().getPageNumber() + 1);
            map.put("pageSize", bookings.getSize());
            map.put("totalPages", bookings.getTotalPages());
            map.put("totalElements", bookings.getTotalElements());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy tất cả vé đã đặt thành công.")
                            .result(map)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<?> getTotalRevenueOfCinemaManager(String managerId) {
        try {
            Optional<Manager> manager = managerRepository.findById(managerId);
            if (manager.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Quản lý chưa được thêm rạp phim.")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            List<Room> rooms = roomRepository.findAllByCinema_CinemaId(manager.get().getCinema().getCinemaId());
            List<ShowTime> showTimes = showTimeRepository.findAllByRoomIn(rooms);
            List<String> showtimeIds = showTimes.stream().map(ShowTime::getShowTimeId).toList();
            List<Booking> bookings = bookingRepository.findAllByShowtimeIdIn(showtimeIds);
            int total = calculateTotalRevenue(bookings);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy tổng doanh thu theo rạp của quản lý thành công.")
                            .result(total)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<?> getTotalRevenueYearOfCinemaManager(String managerId, int year) {
        try {
            Optional<Manager> manager = managerRepository.findById(managerId);
            if (manager.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Quản lý chưa được thêm rạp phim.")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }

            List<Room> rooms = roomRepository.findAllByCinema_CinemaId(manager.get().getCinema().getCinemaId());
            List<ShowTime> showTimes = showTimeRepository.findAllByRoomIn(rooms);
            List<String> showtimeIds = showTimes.stream().map(ShowTime::getShowTimeId).toList();
            List<Integer> temp = getTotalRevenueByYear(year, showtimeIds);
            RevenueOfYearRes res = new RevenueOfYearRes(manager.get().getCinema().getCinemaName(), temp);


            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy tổng doanh thu theo rạp của quản lý thành công.")
                            .result(res)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    // Tổng doanh thu theo năm
    public List<Integer> getTotalRevenueByYear(int year, List<String> showTimeIds) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = calendar.getTime();

        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        Date endDate = calendar.getTime();

        List<Booking> bookings = bookingRepository.findByYearAndShowtimeIds(startDate, endDate, showTimeIds);
        return calculateRevenueByMonths(bookings);
    }

    // Hàm tính doanh thu từng tháng
    private List<Integer> calculateRevenueByMonths(List<Booking> bookings) {
        List<Integer> revenueByMonths = new ArrayList<>(12);

        for (int i = 0; i < 12; i++) {
            revenueByMonths.add(0);
        }

        for (Booking booking : bookings) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(booking.getCreateAt());
            int month = calendar.get(Calendar.MONTH);
            int total = booking.getTotal();
            revenueByMonths.set(month, revenueByMonths.get(month) + total);
        }

        return revenueByMonths;
    }

    public ResponseEntity<GenericResponse> getTotalRevenueOfYear(int year) {
        try {
            List<Cinema> cinemas = cinemaRepository.findAll();
            List<Object> list = new ArrayList<>();
            for (Cinema item : cinemas) {
                List<Room> rooms = roomRepository.findAllByCinema_CinemaId(item.getCinemaId());
                List<ShowTime> showTimes = showTimeRepository.findAllByRoomIn(rooms);
                List<String> showtimeIds = showTimes.stream().map(ShowTime::getShowTimeId).toList();
                List<Integer> total = getTotalRevenueByYear(year, showtimeIds);
                RevenueOfYearRes res = new RevenueOfYearRes(item.getCinemaName(), total);
                list.add(res);
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy tổng doanh thu theo năm của rạp phim thành công.")
                            .result(list)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<?> getTotalRevenue() {
        try {
            int total = calculateTotalRevenue(bookingRepository.findAllByIsPaymentIsTrue());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy tổng doanh thu thành công!")
                            .result(total)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> getTotalRevenueByCinemas() {
        try {
            List<Cinema> cinemas = cinemaRepository.findAll();
            List<RevenueOfCinemasRes> revenue = new ArrayList<>();
            for (Cinema item : cinemas) {
                List<Room> rooms = roomRepository.findAllByCinema_CinemaId(item.getCinemaId());
                List<ShowTime> showTimes = showTimeRepository.findAllByRoomIn(rooms);
                List<String> showtimeIds = showTimes.stream().map(ShowTime::getShowTimeId).toList();
                List<Booking> bookings = bookingRepository.findAllByShowtimeIdIn(showtimeIds);
                int temp = calculateTotalRevenue(bookings);
                revenue.add(new RevenueOfCinemasRes(item.getCinemaName(), temp));
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy tổng doanh thu thành công!")
                            .result(revenue)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
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
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Đã xác nhận vé.")
                            .result(booking)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
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
                                .message("Vé đã quá thời hạn để hủy.")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .build());
            }
            booking.get().setTicketStatus(TicketStatus.CANCELLED);
            bookingRepository.save(booking.get());

            for (FoodWithCount item: booking.get().getFoods()) {
                Food food = item.getFood();
                food.setQuantity(food.getQuantity() + item.getCount());
                foodRepository.save(food);
                Cinema cinema = booking.get().getSeats().get(0).getShowTime().getRoom().getCinema();
                FoodInventory foodInventory = foodInventoryRepository.findByFoodAndCinema(food, cinema).get();
                foodInventory.setQuantity(foodInventory.getQuantity() + item.getCount());
                foodInventoryRepository.save(foodInventory);
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Đã hủy vé thành công!")
                            .result(booking)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
}
