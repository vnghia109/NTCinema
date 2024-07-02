package vn.iostar.NT_cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.service.ShowTimeService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/showtimes")
public class ShowtimeController {
    @Autowired
    ShowTimeService showTimeService;
    @GetMapping("")
    public ResponseEntity<GenericResponse> getShowTimes(@RequestParam(defaultValue = "1") int index,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                        @RequestParam(required = false) String movieId) {
        return showTimeService.getShowTimes(movieId, date, PageRequest.of(index-1, size));
    }
    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse> getShowTime(@PathVariable("id") String id){
        return showTimeService.getShowtime(id);
    }
}
