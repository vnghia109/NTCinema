package vn.iostar.NT_cinema.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.oauth2.resourceserver.OAuth2ResourceServerSecurityMarker;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.entity.Movie;
import vn.iostar.NT_cinema.repository.UserRepository;
import vn.iostar.NT_cinema.service.*;

import java.util.List;
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
    @Autowired
    MovieService movieService;
    @Autowired
    ShowTimeService showTimeService;
    @Autowired
    RoomService roomService;
    @Autowired
    ManagerService managerService;
    @Autowired
    BookingService bookingService;
    @Autowired
    ReviewService reviewService;
    @Autowired
    TicketService ticketService;

    @PostMapping("/managers")
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

    @GetMapping("/managers")
    public ResponseEntity<GenericResponse> getManagers(@RequestParam(defaultValue = "1") int index,
                                                    @RequestParam(defaultValue = "10") int size){
        return managerService.getAllManager(PageRequest.of(index-1, size));
    }

    @GetMapping("/managers/{id}")
    public ResponseEntity<GenericResponse> getManager(@PathVariable("id") String id){
        return managerService.getManager(id);
    }

    @GetMapping("/users")
    public ResponseEntity<GenericResponse> getUsers(@RequestParam(defaultValue = "1") int index,
                                                    @RequestParam(defaultValue = "10") int size){
        return userService.getAllUser(PageRequest.of(index-1, size));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<GenericResponse> getUser(@PathVariable("userId") String id){
        return userService.getUser(id);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<GenericResponse> updateUser(@PathVariable("userId") String id,
                                                      @RequestBody UpdateUserReq userReq,
                                                      BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new GenericResponse(
                    false,
                    "Invalid input data!",
                    null,
                    HttpStatus.BAD_REQUEST.value()));
        }
        return userService.adminUpdateUser(id, userReq);
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<GenericResponse> updateIsDeleteUser(@PathVariable("userId") String id){
        return userService.updateIsDeleteUser(id);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<GenericResponse> deleteUser(@PathVariable("userId") String id){
        return userService.deleteUser(id);
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

    @PatchMapping("/cinemas/{cinemaId}")
    public ResponseEntity<GenericResponse> updateIsDeleteCinema(@PathVariable("cinemaId") String cinemaId){
        return cinemaService.updateIsDeleteCinema(cinemaId);
    }

    @GetMapping("/cinemas")
    public ResponseEntity<GenericResponse> getAllCinema(@RequestParam(defaultValue = "1") int index,
                                                        @RequestParam(defaultValue = "10") int size){
        return cinemaService.adminGetAllCinema(PageRequest.of(index-1, size));
    }

    @GetMapping("/cinemas/{id}")
    public ResponseEntity<GenericResponse> getCinema(@PathVariable("id") String id){
        return cinemaService.getCinema(id);
    }

    @PostMapping("/movies/movie")
    public ResponseEntity<GenericResponse> addMovie(@Valid @ModelAttribute MovieReq movie,
                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new GenericResponse(
                    false,
                    "Invalid input data!",
                    null,
                    HttpStatus.BAD_REQUEST.value()));
        }
        return movieService.save(movie);
    }

    @PutMapping("/movies/{movieId}")
    public ResponseEntity<GenericResponse> updateMovie(@PathVariable("movieId") String movieId,
                                                       @Valid @ModelAttribute MovieReq movieRequest,
                                                       BindingResult bindingResult) throws Exception{
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new GenericResponse(
                    false,
                    "Invalid input data!",
                    null,
                    HttpStatus.BAD_REQUEST.value()));
        }
        return movieService.update(movieId, movieRequest);
    }

    @DeleteMapping("/movies/{movieId}")
    public ResponseEntity<GenericResponse> deleteMovie(@PathVariable("movieId") String movieId) throws Exception{
        return movieService.delete(movieId);
    }

    @PatchMapping("/movies/{movieId}")
    public ResponseEntity<GenericResponse> updateIsDeleteMovie(@PathVariable("movieId") String movieId) throws Exception{
        return movieService.updateIsDelete(movieId);
    }

    @GetMapping("/movies")
    public ResponseEntity<GenericResponse> getAllMovies(@RequestParam(defaultValue = "1") int index,
                                                        @RequestParam(defaultValue = "10") int size) {
        return movieService.adminGetAllMovie(PageRequest.of(index-1, size));
    }

    @PostMapping("/foods/food")
    public ResponseEntity<GenericResponse> addFood(@RequestBody FoodReq foodReq){
        return foodService.addFood(foodReq);
    }

    @DeleteMapping("/foods/{id}")
    public ResponseEntity<GenericResponse> deleteFood(@PathVariable("id") String id){
        return foodService.deleteFood(id);
    }

    @PatchMapping("/foods/{id}")
    public ResponseEntity<GenericResponse> updateIsDeleteFood(@PathVariable("id") String id){
        return foodService.updateIsDeleteFood(id);
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

    @PutMapping("/prices/price/{id}")
    public ResponseEntity<?> updatePrice(@PathVariable("id") String id, @RequestBody PriceReq priceReq){
        return priceService.updatePrice(id, priceReq);
    }

    @GetMapping("/showtimes")
    public ResponseEntity<GenericResponse> getShowTimes(@RequestParam(defaultValue = "1") int index,
                                                        @RequestParam(defaultValue = "10") int size){
        return showTimeService.adminGetShowTimes(PageRequest.of(index-1, size));
    }

    @GetMapping("/showtimes/{id}")
    public ResponseEntity<GenericResponse> getShowTime(@PathVariable("id") String id){
        return showTimeService.getShowtime(id);
    }

    @GetMapping("/rooms")
    public ResponseEntity<GenericResponse> getAllRoom(@RequestParam(defaultValue = "1") int index,
                                                      @RequestParam(defaultValue = "10") int size){
        return roomService.getRooms(PageRequest.of(index-1, size));
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<GenericResponse> getRoom(@PathVariable("id") String id){
        return roomService.getRoom(id);
    }

    @GetMapping("/cinemas/unmanaged")
    public ResponseEntity<GenericResponse> getAllCinemaUnmanaged(){
        return cinemaService.getAllCinemaUnmanaged();
    }

    @PutMapping("/managers/{managerId}/cinema/{cinemaId}")
    public ResponseEntity<GenericResponse> updateCinemaManager(@PathVariable("managerId") String userId,
                                                               @PathVariable("cinemaId") String cinemaId){
        return managerService.updateCinemaManager(userId, cinemaId);
    }

    @PostMapping("/total-revenue")
    public ResponseEntity<?> getTotalRevenueDay(@RequestBody TotalRevenueReq req) {
        return bookingService.getBookingsInDateRange(req.getStartDate(), req.getEndDate());
    }

    @GetMapping("/total-revenue")
    public ResponseEntity<?> getTotalRevenue() {
        return bookingService.getTotalRevenue();
    }

    @GetMapping("/reviews")
    public ResponseEntity<GenericResponse> getReviews(@RequestParam(defaultValue = "1") int index,
                                                    @RequestParam(defaultValue = "10") int size){
        return reviewService.getReviews(PageRequest.of(index-1, size));
    }

    @GetMapping("/bookings")
    public ResponseEntity<GenericResponse> getBookings(@RequestParam(defaultValue = "1") int index,
                                                       @RequestParam(defaultValue = "10") int size){
        return bookingService.getBookings(PageRequest.of(index-1, size));
    }

    @GetMapping("/tickets")
    public ResponseEntity<GenericResponse> getTickets(@RequestParam(defaultValue = "1") int index, 
                                                      @RequestParam(defaultValue = "10") int size){
        return ticketService.getTickets(PageRequest.of(index-1, size));
    }

    @GetMapping("/total-tickets")
    public ResponseEntity<GenericResponse> getTotalTickets() {
        return ticketService.getTotalTickets();
    }

    @GetMapping("/cinemas/total-tickets")
    public ResponseEntity<GenericResponse> getTotalTicketsByCinema(@RequestParam("cinemaId") String id) {
        return ticketService.getTotalTicketsByCinema(id);
    }

    @PostMapping("/tickets/dates")
    public ResponseEntity<GenericResponse> getTicketsSoldBetweenDates(@RequestBody TotalRevenueReq req) {
        return ticketService.getTicketsSoldBetweenDates(req.getStartDate(), req.getEndDate());
    }

    @GetMapping("/year/total-revenue")
    public ResponseEntity<GenericResponse> getTotalRevenueOfYear(@RequestParam("year") int year){
        return bookingService.getTotalRevenueOfYear(year);
    }

    @GetMapping("/year/total-ticket")
    public ResponseEntity<GenericResponse> getTotalTicketsByYear(@RequestParam("year") int year){
        return ticketService.getTotalTicketsByYear(year);
    }

    @GetMapping("/cinemas/total-revenue")
    public ResponseEntity<GenericResponse> getTotalRevenueByCinemas() {
        return bookingService.getTotalRevenueByCinemas();
    }
}
