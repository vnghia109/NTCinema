package vn.iostar.NT_cinema.service;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
                                        .message("Seat already book")
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
            booking.setCreateAt(new Date());
            booking.setSeats(seats);
            booking.setFoods(convertToFoodWithCountList(foodIds));
            booking.setTotal(totalBooking(seats, foods));

            Booking bookingRes = bookingRepository.save(booking);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Confirmed information successfully")
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
                                        .message("Seat already book")
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
                            .message("Get booking info success")
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
            ShowTime showTime = showTimeRepository.findById(booking.getSeats().get(0).getShowTimeId()).get();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(user.get().getEmail());

            // Load Thymeleaf template
            Context context = new Context();
            context.setVariable("bookingCode", booking.getBookingId());
            context.setVariable("movieName", showTime.getMovie().getTitle());
            context.setVariable("showtime", booking.getSeats().get(0).getTimeShow());
            context.setVariable("ticketCount", booking.getSeats().size());
            context.setVariable("foods", booking.getFoods());
            context.setVariable("seats", booking.getSeats());
            context.setVariable("totalAmount", booking.getTotal());
            String mailContent = templateEngine.process("booking-success", context);

            helper.setText(mailContent, true);
            helper.setSubject("Booking ticket success TNCinemas");
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
                                .message("Booking not found")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            Optional<ShowTime> showTime = showTimeRepository.findById(booking.get().getSeats().get(0).getShowTimeId());
            TicketDetailRes ticket = new TicketDetailRes();
            ticket.setMovieId(showTime.get().getMovie().getMovieId());
            ticket.setMovieName(showTime.get().getMovie().getTitle());
            ticket.setTimeShow(booking.get().getSeats().get(0).getTimeShow());
            ticket.setCinemaName(showTime.get().getRoom().getCinema().getCinemaName());
            ticket.setDuration(Integer.parseInt(showTime.get().getMovie().getDuration()));
            ticket.setRoomName(showTime.get().getRoom().getRoomName());
            ticket.setSeats(booking.get().getSeats());
            ticket.setFoods(booking.get().getFoods());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Get ticket detail success")
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
}
