package vn.iostar.NT_cinema.controller.staff;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.StaffReq;
import vn.iostar.NT_cinema.dto.ViewerReq;
import vn.iostar.NT_cinema.service.BookingService;
import vn.iostar.NT_cinema.service.UserService;

@RestController
@PreAuthorize("hasRole('STAFF')")
@RequestMapping("/api/v1/staff")
public class StaffController {
    @Autowired
    UserService userService;
    @Autowired
    BookingService bookingService;

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
                                                      @RequestParam(defaultValue = "") String status) {
        return bookingService.getBookings(PageRequest.of(index-1, size), status);
    }

    @PutMapping("/bookings/{bookingId}/confirm")
    public ResponseEntity<GenericResponse> confirmBooking(@PathVariable("bookingId") String bookingId){
        return bookingService.confirmBooking(bookingId);
    }
}
