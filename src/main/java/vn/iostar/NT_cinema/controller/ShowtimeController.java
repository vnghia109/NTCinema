package vn.iostar.NT_cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.service.ShowTimeService;

@RestController
@RequestMapping("/api/v1/showtimes")
public class ShowtimeController {
    @Autowired
    ShowTimeService showTimeService;
    @GetMapping("")
    public ResponseEntity<GenericResponse> getShowTimes(@RequestParam(defaultValue = "1") int index,
                                                        @RequestParam(defaultValue = "10") int size){
        return showTimeService.getShowTimes(PageRequest.of(index-1, size));
    }
    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse> getShowTime(@PathVariable("id") String id){
        return showTimeService.getShowtime(id);
    }
}
