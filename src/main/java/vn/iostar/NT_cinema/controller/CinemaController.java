package vn.iostar.NT_cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.service.CinemaService;

@RestController
@RequestMapping("/api/v1/cinemas")
public class CinemaController {
    @Autowired
    CinemaService cinemaService;

    @GetMapping
    public ResponseEntity<GenericResponse> getAllCinema() {
        return cinemaService.getAllCinema();
    }

    @GetMapping("/{Id}")
    public ResponseEntity<GenericResponse> getCinema(@PathVariable("Id") String Id) {
        return cinemaService.findById(Id);
    }
}