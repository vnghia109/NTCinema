package vn.iostar.NT_cinema.controller.admin;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.service.*;

import java.time.LocalDate;
import java.time.LocalTime;
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
    @Autowired
    ScheduleService scheduleService;
    @Autowired
    SeatService seatService;
    @Autowired
    PromotionService promotionService;
    @Autowired
    StatsService statsService;
    @Autowired
    GenresService genresService;
    @Autowired
    StockEntryService stockEntryService;
    @Autowired
    NotificationService notificationService;

    @PostMapping("/managers")
    public ResponseEntity<GenericResponse> addManager(@Valid @RequestBody ManagerRequest request,
                                                      BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new GenericResponse(
                    false,
                    "Dữ liệu đầu vào không hợp lệ!!",
                    null,
                    HttpStatus.BAD_REQUEST.value()));
        }
        return userService.addManager(request);
    }

    @PostMapping("/staff")
    public ResponseEntity<GenericResponse> addStaff(@Valid @RequestBody StaffReq request,
                                                      BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new GenericResponse(
                    false,
                    "Dữ liệu đầu vào không hợp lệ!!",
                    null,
                    HttpStatus.BAD_REQUEST.value()));
        }
        return userService.addStaff(request);
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

    @GetMapping("/personnel")
    public ResponseEntity<GenericResponse> getPersonnel(@RequestParam(defaultValue = "1") int index,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) boolean sortByRole,
                                                        @RequestParam(required = false) String search){
        return userService.getAllPersonnel(sortByRole, search, PageRequest.of(index-1, size));
    }

    @GetMapping("/viewers")
    public ResponseEntity<GenericResponse> getViewers(@RequestParam(defaultValue = "1") int index,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) String search){
        return userService.getAllViewer(search, PageRequest.of(index-1, size));
    }

    @GetMapping("/users")
    public ResponseEntity<GenericResponse> getUsers(@RequestParam(defaultValue = "1") int index,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(defaultValue = "ALL") String role){
        return userService.getAllUser(role, PageRequest.of(index-1, size));
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
                    "Dữ liệu đầu vào không hợp lệ!!",
                    null,
                    HttpStatus.BAD_REQUEST.value()));
        }
        return userService.adminUpdateUser(id, userReq);
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<GenericResponse> updateIsDeleteUser(@PathVariable("userId") String id){
        return userService.updateIsDeleteUser(id);
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
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(defaultValue = "true") boolean status){
        return cinemaService.adminGetAllCinema(status, PageRequest.of(index-1, size));
    }

    @GetMapping("/cinemas/{id}")
    public ResponseEntity<GenericResponse> getCinema(@PathVariable("id") String id){
        return cinemaService.getCinema(id);
    }

    @GetMapping("/cinemas/{Id}/showtimes")
    public ResponseEntity<GenericResponse> getShowtimesOfCinema(@PathVariable("Id") String Id,
                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                @RequestParam(defaultValue = "1") int index,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                @RequestParam(required = false) String movieId) {
        return showTimeService.findShowtimesByCinema(Id, date, movieId, PageRequest.of(index-1, size));
    }

    @GetMapping("/cinemas/{Id}/rooms")
    public ResponseEntity<GenericResponse> getRooms(@PathVariable("Id") String Id,
                                                    @RequestParam(defaultValue = "1") int index,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(defaultValue = "false") boolean isDelete) {
        return roomService.findRoomsByCinema(isDelete, Id, PageRequest.of(index-1, size));
    }

    @GetMapping("/rooms/{Id}/showtimes")
    public ResponseEntity<GenericResponse> getShowtimesOfRoom(@PathVariable("Id") String Id,
                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                              @RequestParam(defaultValue = "1") int index,
                                                              @RequestParam(defaultValue = "10") int size,
                                                              @RequestParam(required = false) String movieId) {
        return showTimeService.findShowtimesByRoom(Id, date, movieId, PageRequest.of(index-1, size));
    }

    @PostMapping("/movies/movie")
    public ResponseEntity<GenericResponse> addMovie(@Valid @ModelAttribute MovieReq movie,
                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new GenericResponse(
                    false,
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(),
                    "Dữ liệu đầu vào không đúng định dạng!",
                    HttpStatus.BAD_REQUEST.value()));
        }
        return movieService.save(movie);
    }

    @PutMapping("/movies/{movieId}")
    public ResponseEntity<GenericResponse> updateMovie(@PathVariable("movieId") String movieId,
                                                       @Valid @ModelAttribute UpdateMovieReq movieRequest,
                                                       BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new GenericResponse(
                    false,
                    "Dữ liệu đầu vào không đúng định dạng!",
                    null,
                    HttpStatus.BAD_REQUEST.value()));
        }
        return movieService.update(movieId, movieRequest);
    }

    @PatchMapping("/movies/{movieId}")
    public ResponseEntity<GenericResponse> updateIsDeleteMovie(@PathVariable("movieId") String movieId){
        return movieService.updateIsDelete(movieId);
    }

    @GetMapping("/movies")
    public ResponseEntity<GenericResponse> getAllMovies(@RequestParam(defaultValue = "1") int index,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String genresId) {
        return movieService.adminGetAllMovie(genresId, PageRequest.of(index-1, size));
    }

    @PostMapping("/foods/food")
    public ResponseEntity<GenericResponse> addFood(@Valid @ModelAttribute FoodReq foodReq){
        return foodService.addFood(foodReq);
    }

    @GetMapping("/foods")
    public ResponseEntity<GenericResponse> getFoods(@RequestParam(defaultValue = "") String type,
                                                    @RequestParam(defaultValue = "1") int index,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(defaultValue = "true") boolean status) {
        return foodService.adminGetFoods(status, type, PageRequest.of(index-1, size));
    }

    @PatchMapping("/foods/{id}")
    public ResponseEntity<GenericResponse> updateIsDeleteFood(@PathVariable("id") String id){
        return foodService.updateIsDeleteFood(id);
    }

    @PutMapping("/foods/{id}")
    public ResponseEntity<GenericResponse> updateFood(@PathVariable("id") String id,
                                                      @Valid @ModelAttribute FoodReq foodReq){
        return foodService.updateFood(id, foodReq);
    }

    @GetMapping("/prices")
    public ResponseEntity<GenericResponse> getListPrice(){
        return priceService.getAllPrice();
    }
    

    @PutMapping("/prices/price/{id}")
    public ResponseEntity<?> updatePrice(@PathVariable("id") String id, @RequestBody PriceReq priceReq){
        return priceService.updatePrice(id, priceReq);
    }

    @GetMapping("/showtimes")
    public ResponseEntity<GenericResponse> getShowTimes(@RequestParam(defaultValue = "1") int index,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                        @RequestParam(required = false) String movieId){
        return showTimeService.adminGetShowTimes(date, movieId, PageRequest.of(index-1, size));
    }

    @GetMapping("/showtimes/schedule/check")
    public ResponseEntity<GenericResponse> checkScheduleBeforeAddShowtime(@RequestParam("roomId") String roomId,
                                                         @RequestParam("movieId") String movieId,
                                                         @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                         @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime){
        return scheduleService.checkScheduleBeforeAddShowtime(roomId, movieId, date, startTime);
    }

    @PostMapping("/showtimes/showtime")
    public ResponseEntity<GenericResponse> addShowTime(@RequestBody ShowTimeReq showTimeReq){
        return showTimeService.addShowTime(showTimeReq);
    }

    @PutMapping("/showtimes/{id}")
    public ResponseEntity<GenericResponse> updateShowTime(@PathVariable("id") String id,
                                                          @RequestBody UpdateShowTimeReq showTimeReq){
        return showTimeService.updateShowTime(id, showTimeReq);
    }

    @PatchMapping("/showtimes/{id}")
    public ResponseEntity<GenericResponse> updateIsDeleteShowTime(@PathVariable("id") String id){
        return showTimeService.updateIsDeleteShowTime(id);
    }

    @PostMapping("/schedule")
    public ResponseEntity<GenericResponse> addSchedule(@RequestBody AddScheduleReq scheduleReq){
        return scheduleService.addSchedule(scheduleReq);
    }

    @GetMapping("/schedule/check")
    public ResponseEntity<GenericResponse> checkSchedule(@RequestParam("showtimeId") String showtimeId,
                                                         @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                         @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime){
        return scheduleService.checkSchedule(showtimeId, date, startTime);
    }

    @DeleteMapping("/schedule/{id}")
    public ResponseEntity<GenericResponse> deleteSchedule(@PathVariable("id") String id){
        return scheduleService.deleteSchedule(id);
    }

    @GetMapping("/showtimes/{id}")
    public ResponseEntity<GenericResponse> getShowTime(@PathVariable("id") String id){
        return showTimeService.getShowtime(id);
    }

    @GetMapping("/seats-booked/count")
    public ResponseEntity<GenericResponse> countSeatBooked(@RequestParam("showtimeId") String showtimeId,
                                                           @RequestParam("scheduleId") String scheduleId){
        return seatService.countSeatBooked(showtimeId, scheduleId);
    }

    @GetMapping("/rooms")
    public ResponseEntity<GenericResponse> getAllRoom(@RequestParam(defaultValue = "1") int index,
                                                      @RequestParam(defaultValue = "10") int size) {
        return roomService.getRooms(PageRequest.of(index-1, size));
    }

    @PostMapping("/rooms/room")
    public ResponseEntity<GenericResponse> addRoom(@RequestBody RoomReq roomReq){
        return roomService.addRoomByAdmin(roomReq);
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

    @GetMapping("/reviews")
    public ResponseEntity<GenericResponse> getReviews(@RequestParam(defaultValue = "1") int index,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) String movieId,
                                                      @RequestParam(required = false) Integer star) {
        return reviewService.getReviews(movieId, star, PageRequest.of(index-1, size));
    }

    @GetMapping("/bookings")
    public ResponseEntity<GenericResponse> getBookings(@RequestParam(defaultValue = "1") int index,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(defaultValue = "") String status,
                                                       @RequestParam(required = false) String cinemaId){
        return bookingService.getBookings(status, cinemaId, PageRequest.of(index-1, size));
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

    @GetMapping("/stats/overview")
    public ResponseEntity<GenericResponse> getStatsOverview() {
        return statsService.getStatsOverview();
    }

    @GetMapping("/stats/top-cinemas")
    public ResponseEntity<GenericResponse> getTopCinemasRevenue() {
        return statsService.getTopCinemasRevenue();
    }

    @GetMapping("/total-revenue")
    public ResponseEntity<GenericResponse> getTotalRevenueOfYear(@RequestParam(required = false) Integer year,
                                                                 @RequestParam(required = false) Integer month,
                                                                 @RequestParam(defaultValue = "false") boolean isTicket) {
        return statsService.getRevenueStats(year, month, isTicket);
    }

    @GetMapping("/top-rated-movies")
    public ResponseEntity<GenericResponse> getTopRatedMovies(@RequestParam(defaultValue = "5") int top) {
        return movieService.findTopMovie(top);
    }

    @GetMapping("/top-users")
    public ResponseEntity<GenericResponse> getTopUsers(@RequestParam(defaultValue = "5") int top,
                                                       @RequestParam(defaultValue = "false") boolean isStaff) {
        return statsService.getTopUsers(top, isStaff);
    }

    @GetMapping("/finance")
    public ResponseEntity<GenericResponse> getFinance(@RequestParam() int year) {
        return statsService.getFinance(year);
    }

    @GetMapping("/finance/detail")
    public ResponseEntity<GenericResponse> getFinanceDetail(@RequestParam("cinemaId") String cinemaId,
                                                            @RequestParam("year") Integer year) {
        return statsService.getFinanceDetail(cinemaId, year);
    }

    @GetMapping("/year/total-ticket")
    public ResponseEntity<GenericResponse> getTotalTicketsByYear(@RequestParam("year") int year){
        return ticketService.getTotalTicketsByYear(year);
    }

    @GetMapping("/promotions")
    public ResponseEntity<GenericResponse> getAllPromotions(@RequestParam(defaultValue = "1") int index,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            @RequestParam(defaultValue = "false") boolean isFixed,
                                                            @RequestParam(required = false) String code,
                                                            @RequestParam(required = false) String name) {
        return promotionService.getAllPromotions(isFixed, code, name, PageRequest.of(index-1, size));
    }

    @GetMapping("/promotions/{id}")
    public ResponseEntity<GenericResponse> getPromotion(@PathVariable String id) {
        return promotionService.getPromotion(id);
    }

    @PostMapping("/promotionsFixed")
    public ResponseEntity<GenericResponse> createPromotionFixed(@Valid @ModelAttribute PromotionFixedReq promotionFixedReq) {
        return promotionService.createPromotionFixed(promotionFixedReq);
    }

    @PostMapping("/promotionsCode")
    public ResponseEntity<GenericResponse> createPromotionCode(@Valid @ModelAttribute PromotionCodeReq promotionCodeReq) {
        return promotionService.createPromotionCode(promotionCodeReq);
    }

    @PutMapping("/promotionsFixed/{id}")
    public ResponseEntity<GenericResponse> updatePromotionFixed(@PathVariable String id,
                                                                @Valid @ModelAttribute PromotionFixedReq promotionFixedReq) {
        return promotionService.updatePromotionFixed(id, promotionFixedReq);
    }

    @PutMapping("/promotionsCode/{id}")
    public ResponseEntity<GenericResponse> updatePromotionCode(@PathVariable String id,
                                                           @Valid @ModelAttribute PromotionCodeReq promotionCodeReq) {
        return promotionService.updatePromotionCode(id, promotionCodeReq);
    }

    @PatchMapping("/promotionsFixed/{id}")
    public ResponseEntity<GenericResponse> deletePromotionFixed(@PathVariable String id) {
        return promotionService.deletePromotionFixed(id);
    }

    @PatchMapping("/promotionsCode/{id}")
    public ResponseEntity<GenericResponse> deletePromotionCode(@PathVariable String id) {
        return promotionService.deletePromotionCode(id);
    }

    @GetMapping("/genres")
    public ResponseEntity<GenericResponse> getGenres() {
        return genresService.getGenres();
    }

    @PostMapping("/genres")
    public ResponseEntity<GenericResponse> createGenres(@RequestBody GenresReq genres) {
        return genresService.createGenres(genres);
    }

    @DeleteMapping("/genres/{id}")
    public ResponseEntity<GenericResponse> deleteGenres(@PathVariable String id) {
        return genresService.deleteGenres(id);
    }

    @GetMapping("/stockEntries")
    public ResponseEntity<GenericResponse> getStockEntries(@RequestParam(required = false) String cinemaId,
                                                           @RequestParam(defaultValue = "1") int index,
                                                           @RequestParam(defaultValue = "10") int size){
        return stockEntryService.getStockEntriesByCinema(PageRequest.of(index-1, size), cinemaId);
    }

    @PostMapping("/notification/send")
    public ResponseEntity<GenericResponse> sendNotification(@RequestBody NotificationReq notificationReq) {
        return notificationService.adminSendNotification(notificationReq);
    }
}
