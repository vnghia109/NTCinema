package vn.iostar.NT_cinema.controller.staff;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.SellTicketReq;
import vn.iostar.NT_cinema.dto.ViewerReq;
import vn.iostar.NT_cinema.security.JwtTokenProvider;
import vn.iostar.NT_cinema.service.BookingService;
import vn.iostar.NT_cinema.service.MovieService;
import vn.iostar.NT_cinema.service.UserService;

@RestController
@PreAuthorize("hasRole('STAFF')")
@RequestMapping("/api/v1/staff")
public class StaffController {
    @Autowired
    UserService userService;
    @Autowired
    BookingService bookingService;
    @Autowired
    MovieService movieService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @PostMapping("/viewer")
    public ResponseEntity<GenericResponse> addViewer(@Valid @RequestBody ViewerReq request){
        if (request.getPhone().isEmpty() && request.getEmail().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                            GenericResponse.builder()
                                    .success(false)
                                    .message("Để tạo tài khoản cần có SDT hoặc email.")
                                    .result(null)
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build()
                    );
        }
        return userService.addViewer(request);
    }

    @GetMapping("/bookings")
    public ResponseEntity<GenericResponse> getBookings(@RequestParam(defaultValue = "1") int index,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(defaultValue = "") String status,
                                                      @RequestParam(required = false) String cinemaId) {
        return bookingService.getBookings(status, cinemaId, PageRequest.of(index - 1, size));
    }

    @PutMapping("/bookings/{bookingId}/confirm")
    public ResponseEntity<GenericResponse> confirmBooking(@PathVariable("bookingId") String bookingId){
        return bookingService.confirmBooking(bookingId);
    }

    @GetMapping("/movie/now-playing")
    public ResponseEntity<GenericResponse> getNowPlayingAndSpecialMovies(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String staffId = jwtTokenProvider.getUserIdFromJwt(token);
        return movieService.findNowPlayingMoviesAndSpecialByStaff(staffId);
    }

    @GetMapping("/movie/coming-soon")
    public ResponseEntity<GenericResponse> getComingSoonMovies(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String staffId = jwtTokenProvider.getUserIdFromJwt(token);
        return movieService.findComingSoonMoviesByStaff(staffId);
    }

    @GetMapping("/search/viewer")
    public ResponseEntity<GenericResponse> searchViewers(@RequestParam("keyWord") String keyWord) {
        return userService.searchViewers(keyWord);
    }

    @PostMapping("/sell-ticket")
    public ResponseEntity<GenericResponse> sellTicket(@RequestHeader("Authorization") String authorizationHeader,
                                                      @RequestBody SellTicketReq request) {
        String token = authorizationHeader.substring(7);
        String staffId = jwtTokenProvider.getUserIdFromJwt(token);
        return bookingService.sellTicket(staffId, request);
    }
}
