package vn.iostar.NT_cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.service.CinemaService;
import vn.iostar.NT_cinema.service.MovieService;

@RestController
@RequestMapping("/api/v1/cinemas")
public class CinemaController {
    @Autowired
    CinemaService cinemaService;

    @Autowired
    MovieService movieService;
    @GetMapping
    public ResponseEntity<GenericResponse> getAllCinema(@RequestParam(defaultValue = "1") int index,
                                                        @RequestParam(defaultValue = "10") int size) {
        return cinemaService.getAllCinema(PageRequest.of(index-1, size));
    }

    @GetMapping("/{Id}")
    public ResponseEntity<GenericResponse> getCinema(@PathVariable("Id") String Id) {
        return cinemaService.findById(Id);
    }

    @GetMapping("/{Id}/movies")
    public ResponseEntity<GenericResponse> getMovies(@PathVariable("Id") String Id) {
        return movieService.findNowPlayingMoviesByCinema(Id);
    }
}
