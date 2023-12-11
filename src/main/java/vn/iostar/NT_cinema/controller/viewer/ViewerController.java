package vn.iostar.NT_cinema.controller.viewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.BookReq;
import vn.iostar.NT_cinema.dto.BookedSeatReq;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.SeatReq;
import vn.iostar.NT_cinema.security.JwtTokenProvider;
import vn.iostar.NT_cinema.service.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@RestController
@PreAuthorize("hasRole('VIEWER')")
@RequestMapping("/api/v1/viewer")
public class ViewerController {
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    ViewerService viewerService;

    @Autowired
    FoodService foodService;

    @Autowired
    SeatService seatService;

    @Autowired
    BookingService bookingService;

    @Autowired
    PriceService priceService;

    @PostMapping("/selectSeat/{showTimeId}")
    public ResponseEntity<GenericResponse> checkSeat(@PathVariable("showTimeId") String showTimeId, @RequestBody List<SeatReq> seatReqList){
        return seatService.checkSeat(showTimeId, seatReqList);
    }

    @PostMapping("/book")
    public ResponseEntity<GenericResponse> bookTicket(@RequestHeader("Authorization") String authorizationHeader,
                                                      @RequestBody BookReq bookReq){
        String userId = jwtTokenProvider.getUserIdFromJwt(
                authorizationHeader.substring(7)
        );
        return bookingService.bookTicket(userId, bookReq);
    }

    @PostMapping("/seats/booked")
    public ResponseEntity<GenericResponse> getSeatBooked(@RequestBody BookedSeatReq req){
        return seatService.getSeatBooked(req);
    }

    @GetMapping("/seat/price")
    public ResponseEntity<?> getSeatPrice(@RequestParam("type") String type){
        return priceService.getPriceOfSeat(type);
    }
}
