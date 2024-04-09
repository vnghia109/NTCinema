package vn.iostar.NT_cinema.controller.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.security.JwtTokenProvider;
import vn.iostar.NT_cinema.service.*;

import java.time.LocalDate;

@RestController
@PreAuthorize("hasRole('MANAGER')")
@RequestMapping("/api/v1/manager")
public class ManagerController {
    @Autowired
    UserService userService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    ShowTimeService showTimeService;
    @Autowired
    TicketService ticketService;
    @Autowired
    RoomService roomService;
    @Autowired
    BookingService bookingService;
    @Autowired
    ManagerService managerService;
    @Autowired
    ReviewService reviewService;
    @Autowired
    ScheduleService scheduleService;

    @PostMapping("/rooms")
    public ResponseEntity<GenericResponse> addRoom(@RequestHeader("Authorization") String authorizationHeader,
                                                   @RequestBody RoomReq roomReq){
        String token = authorizationHeader.substring(7);
        String managerId = jwtTokenProvider.getUserIdFromJwt(token);
        return roomService.addRoomByManager(roomReq, managerId);
    }

    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<GenericResponse> updateRoom(@PathVariable("roomId") String roomId,
                                                      @RequestBody UpdateRoomReq updateRoomReq){
        return roomService.updateRoom(roomId, updateRoomReq);
    }

    @PatchMapping("/rooms/{roomId}")
    public ResponseEntity<GenericResponse> updateIsDeleteRoom(@PathVariable("roomId") String roomId){
        return roomService.updateIsDeleteRoom(roomId);
    }

    @GetMapping("/rooms")
    public ResponseEntity<GenericResponse> getAllRoom(@RequestHeader("Authorization") String authorizationHeader,
                                                      @RequestParam(defaultValue = "1") int index,
                                                      @RequestParam(defaultValue = "10") int size){
        String token = authorizationHeader.substring(7);
        String managerId = jwtTokenProvider.getUserIdFromJwt(token);
        return roomService.getRoomsOfManager(managerId, PageRequest.of(index-1, size));
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<GenericResponse> getRoom(@PathVariable("id") String id){
        return roomService.getRoom(id);
    }

    @PostMapping("/showtimes/showtime")
    public ResponseEntity<GenericResponse> addShowTime(@RequestBody ShowTimeReq showTimeReq){
        return showTimeService.addShowTime(showTimeReq);
    }

    @PatchMapping("/showtimes/{id}")
    public ResponseEntity<GenericResponse> updateIsDeleteShowTime(@PathVariable("id") String id){
        return showTimeService.updateIsDeleteShowTime(id);
    }

    @PutMapping("/showtimes/{id}")
    public ResponseEntity<GenericResponse> updateShowTime(@PathVariable("id") String id,
                                                          @RequestBody UpdateShowTimeReq showTimeReq){
        return showTimeService.updateShowTime(id, showTimeReq);
    }

    @PostMapping("/schedule")
    public ResponseEntity<GenericResponse> addSchedule(@RequestBody AddScheduleReq scheduleReq){
        return scheduleService.addSchedule(scheduleReq);
    }

    @DeleteMapping("/schedule/{id}")
    public ResponseEntity<GenericResponse> deleteSchedule(@PathVariable("id") String id){
        return scheduleService.deleteSchedule(id);
    }

    @GetMapping("/showtimes")
    public ResponseEntity<GenericResponse> getShowTimes(@RequestHeader("Authorization") String authorizationHeader,
                                                        @RequestParam(defaultValue = "1") int index,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        String token = authorizationHeader.substring(7);
        String managerId = jwtTokenProvider.getUserIdFromJwt(token);
        return showTimeService.getShowTimesOfManager(date, managerId, PageRequest.of(index-1, size));
    }

    @GetMapping("/showtimes/{id}")
    public ResponseEntity<GenericResponse> getShowTime(@PathVariable("id") String id){
        return showTimeService.getShowtime(id);
    }

    @GetMapping("/rooms/{roomId}/timeShow")
    public ResponseEntity<GenericResponse> getTimeShowOfRoom(@PathVariable String roomId){
        return showTimeService.getTimeShowOfRoom(roomId);
    }

    @GetMapping("/reviews")
    public ResponseEntity<GenericResponse> getReviews(@RequestParam(defaultValue = "1") int index,
                                                      @RequestParam(defaultValue = "10") int size){
        return reviewService.getReviews(PageRequest.of(index-1, size));
    }

    @GetMapping("/tickets")
    public ResponseEntity<GenericResponse> getTickets(@RequestParam(defaultValue = "1") int index,
                                                      @RequestParam(defaultValue = "10") int size){
        return ticketService.getTickets(PageRequest.of(index-1, size));
    }


    @GetMapping("/total-tickets")
    public ResponseEntity<GenericResponse> getTotalTicketsByCinemaOfManager(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String managerId = jwtTokenProvider.getUserIdFromJwt(token);
        return ticketService.getTotalTicketsByCinemaOfManager(managerId);
    }

    @PostMapping("/tickets/dates")
    public ResponseEntity<GenericResponse> getTicketsSoldBetweenDatesOfManager(@RequestBody TotalRevenueReq req,
                                                                               @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String managerId = jwtTokenProvider.getUserIdFromJwt(token);
        return ticketService.getTicketsSoldBetweenDatesOfManager(req.getStartDate(), req.getEndDate(), managerId);
    }

    @GetMapping("/total-revenue")
    public ResponseEntity<?> getTotalRevenueOfCinemaManager(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String managerId = jwtTokenProvider.getUserIdFromJwt(token);
        return bookingService.getTotalRevenueOfCinemaManager(managerId);
    }

    @GetMapping("/year/total-revenue")
    public ResponseEntity<?> getTotalRevenueOfCinemaManager(@RequestHeader("Authorization") String authorizationHeader,
                                                            @RequestParam("year") int year) {
        String token = authorizationHeader.substring(7);
        String managerId = jwtTokenProvider.getUserIdFromJwt(token);
        return bookingService.getTotalRevenueYearOfCinemaManager(managerId, year);
    }

    @PostMapping("/staff")
    public ResponseEntity<GenericResponse> addStaff(@RequestBody StaffReq request,
                                                    BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new GenericResponse(
                    false,
                    "Invalid input data!",
                    null,
                    HttpStatus.BAD_REQUEST.value()));
        }
        return userService.addStaff(request);
    }
}
