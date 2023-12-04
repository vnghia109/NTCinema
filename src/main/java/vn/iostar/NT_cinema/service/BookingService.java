package vn.iostar.NT_cinema.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.BookReq;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.entity.Booking;
import vn.iostar.NT_cinema.entity.Food;
import vn.iostar.NT_cinema.entity.Seat;
import vn.iostar.NT_cinema.repository.BookingRepository;
import vn.iostar.NT_cinema.repository.FoodRepository;
import vn.iostar.NT_cinema.repository.SeatRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    SeatRepository seatRepository;
    @Autowired
    FoodRepository foodRepository;

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
            booking.setFoods(foods);
            booking.setTotal(totalBooking(seats, foods));

            Booking bookingRes = bookingRepository.save(booking);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Booking ticket success")
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
}
