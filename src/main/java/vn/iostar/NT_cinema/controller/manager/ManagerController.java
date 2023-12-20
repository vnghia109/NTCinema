package vn.iostar.NT_cinema.controller.manager;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.security.JwtTokenProvider;
import vn.iostar.NT_cinema.service.*;

@RestController
@PreAuthorize("hasRole('MANAGER')")
@RequestMapping("/api/v1/manager")
public class ManagerController {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    ShowTimeService showTimeService;

    @Autowired
    RoomService roomService;

    @Autowired
    ManagerService managerService;
    @Autowired
    ReviewService reviewService;

    @PostMapping("/rooms")
    public ResponseEntity<GenericResponse> addRoom(@RequestHeader("Authorization") String authorizationHeader,
                                                   @RequestParam String roomName){
        String token = authorizationHeader.substring(7);
        String managerId = jwtTokenProvider.getUserIdFromJwt(token);
        return roomService.addRoom(roomName, managerId);
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<GenericResponse> deleteRoom(@PathVariable("roomId") String roomId){
        return roomService.deleteRoom(roomId);
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

    @DeleteMapping("/showtimes/{id}")
    public ResponseEntity<GenericResponse> deleteShowTime(@PathVariable("id") String id){
        return showTimeService.deleteShowTime(id);
    }

    @PatchMapping("/showtimes/{id}")
    public ResponseEntity<GenericResponse> updateIsDeleteShowTime(@PathVariable("id") String id){
        return showTimeService.updateIsDeleteShowTime(id);
    }

    @PutMapping("/showtimes/{id}")
    public ResponseEntity<GenericResponse> updateShowTime(@PathVariable("id") String id,
                                                          @RequestBody ShowTimeReq showTimeReq){
        return showTimeService.updateShowTime(id, showTimeReq);
    }

    @GetMapping("/showtimes")
    public ResponseEntity<GenericResponse> getShowTimes(@RequestHeader("Authorization") String authorizationHeader,
                                                        @RequestParam(defaultValue = "1") int index,
                                                        @RequestParam(defaultValue = "10") int size){
        String token = authorizationHeader.substring(7);
        String managerId = jwtTokenProvider.getUserIdFromJwt(token);
        return showTimeService.getShowTimesOfManager(managerId, PageRequest.of(index-1, size));
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
}
