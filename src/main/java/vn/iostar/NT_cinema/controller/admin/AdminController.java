package vn.iostar.NT_cinema.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.oauth2.resourceserver.OAuth2ResourceServerSecurityMarker;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.CinemaReq;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.ManagerRequest;
import vn.iostar.NT_cinema.repository.UserRepository;
import vn.iostar.NT_cinema.service.CinemaService;
import vn.iostar.NT_cinema.service.UserService;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin")
public class AdminController {
    @Autowired
    UserService userService;
    @Autowired
    CinemaService cinemaService;

    @PostMapping("/manager")
    public ResponseEntity<GenericResponse> addManager(@RequestBody ManagerRequest request,
                                                      BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new GenericResponse(
                    false,
                    "Invalid input data!",
                    null,
                    HttpStatus.BAD_REQUEST.value()));
        }
        return userService.addManager(request);
    }

    @PostMapping("/cinemas/cinema")
    public ResponseEntity<GenericResponse> addCinema(@RequestBody CinemaReq cinemaReq){
        return cinemaService.addCinema(cinemaReq);
    }

    @PutMapping("/cinemas/{cinemaId}")
    public ResponseEntity<GenericResponse> updateCinema(@PathVariable("cinemaId") String cinemaId,
                                                        @RequestBody CinemaReq cinemaReq){
        return cinemaService.updateCinema(cinemaId, cinemaReq);
    }

    @GetMapping("/cinemas")
    public ResponseEntity<GenericResponse> getAllCinema(){
        return cinemaService.getAllCinema();
    }
}
