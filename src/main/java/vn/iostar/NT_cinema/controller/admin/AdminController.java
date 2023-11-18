package vn.iostar.NT_cinema.controller.admin;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.oauth2.resourceserver.OAuth2ResourceServerSecurityMarker;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.repository.UserRepository;
import vn.iostar.NT_cinema.service.CinemaService;
import vn.iostar.NT_cinema.service.FoodService;
import vn.iostar.NT_cinema.service.PriceService;
import vn.iostar.NT_cinema.service.UserService;

import java.util.Objects;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin")
public class AdminController {
    @Autowired
    UserService userService;
    @Autowired
    CinemaService cinemaService;

    @Autowired
    FoodService foodService;

    @Autowired
    PriceService priceService;

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

    @DeleteMapping("/cinemas/{cinemaId}")
    public ResponseEntity<GenericResponse> deleteCinema(@PathVariable("cinemaId") String cinemaId){
        return cinemaService.deleteCinema(cinemaId);
    }

    @GetMapping("/cinemas")
    public ResponseEntity<GenericResponse> getAllCinema(@RequestParam(defaultValue = "1") int index,
                                                        @RequestParam(defaultValue = "10") int size){
        return cinemaService.getAllCinema(PageRequest.of(index-1, size));
    }

    @PostMapping("/foods/food")
    public ResponseEntity<GenericResponse> addFood(@RequestBody FoodReq foodReq){
        return foodService.addFood(foodReq);
    }

    @DeleteMapping("/foods/{id}")
    public ResponseEntity<GenericResponse> deleteFood(@PathVariable("id") String id){
        return foodService.deleteFood(id);
    }

    @PutMapping("/foods/{id}")
    public ResponseEntity<GenericResponse> updateFood(@PathVariable("id") String id,
                                                      @RequestBody FoodReq foodReq){
        return foodService.updateFood(id, foodReq);
    }

    @GetMapping("/prices")
    public ResponseEntity<GenericResponse> getListPrice(){
        return priceService.getAllPrice();
    }

    @PostMapping("/prices/price")
    public ResponseEntity<GenericResponse> addPrice(@RequestBody @Valid PriceReq priceReq,
                                                    BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            throw new RuntimeException(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        return priceService.addPrice(priceReq);
    }
}
