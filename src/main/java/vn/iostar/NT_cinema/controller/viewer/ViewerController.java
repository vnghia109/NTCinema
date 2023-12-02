package vn.iostar.NT_cinema.controller.viewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.BookReq;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.SeatReq;
import vn.iostar.NT_cinema.security.JwtTokenProvider;
import vn.iostar.NT_cinema.service.BookingService;
import vn.iostar.NT_cinema.service.FoodService;
import vn.iostar.NT_cinema.service.SeatService;
import vn.iostar.NT_cinema.service.ViewerService;

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

    @GetMapping("/foods")
    public ResponseEntity<GenericResponse> getFoods(@RequestParam(defaultValue = "") String type){
        return foodService.getFoods(type);
    }

    @PostMapping("/checkSeat/{showTimeId}")
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

    @GetMapping("/seats/booked/{showtimeId}")
    public ResponseEntity<GenericResponse> getSeatBooked(@PathVariable("showtimeId") String showtimeId){
        return seatService.getSeatBooked(showtimeId);
    }
}
