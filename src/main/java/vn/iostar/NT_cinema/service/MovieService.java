package vn.iostar.NT_cinema.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.ShowStatus;
import vn.iostar.NT_cinema.util.PaginationUtils;
import vn.iostar.NT_cinema.dto.*;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    ShowTimeRepository showTimeRepository;
    @Autowired
    CloudinaryService cloudinaryService;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    StaffRepository staffRepository;
    @Autowired
    GenresRepository genresRepository;
    @Autowired
    MongoTemplate mongoTemplate;

    public ResponseEntity<GenericResponse> allMovies(String genresId, Pageable pageable) {
        List<MovieRes> movieRes;
        Page<Movie> moviePage;
        if (genresId != null && !genresId.isBlank()) {
            Criteria criteria = Criteria.where("isDelete").is(false)
                    .and("genres.id").is(genresId);
            Query query = new Query(criteria);

            List<Movie> movies = mongoTemplate.find(query, Movie.class);
            moviePage = PaginationUtils.paginate(movies, pageable);
            movieRes = moviePage.getContent().stream()
                    .sorted(Comparator.comparing(Movie::getMovieId, Comparator.reverseOrder()))
                    .map(this::mapMovieToMovieRes)
                    .collect(Collectors.toList());
        } else {
        moviePage = movieRepository.findAllByIsDeleteIsFalseOrderByMovieIdDesc(pageable);
        movieRes = moviePage.getContent().stream()
                .map(this::mapMovieToMovieRes)
                .collect(Collectors.toList());
        }

        Map<String, Object> map = new HashMap<>();
        map.put("content", movieRes);
        map.put("pageNumber", moviePage.getPageable().getPageNumber() + 1);
        map.put("pageSize", moviePage.getSize());
        map.put("totalPages", moviePage.getTotalPages());
        map.put("totalElements", moviePage.getTotalElements());

        return ResponseEntity.status(HttpStatus.OK)
                .body(GenericResponse.builder()
                        .success(true)
                        .message("Lấy danh sách phim thành công!")
                        .result(map)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    private MovieRes mapMovieToMovieRes(Movie movie) {
        MovieRes movieRes = new MovieRes();
        movieRes.setMovieId(movie.getMovieId());
        movieRes.setTitle(movie.getTitle());
        movieRes.setDirector(movie.getDirector());
        movieRes.setGenres(movie.getGenres());
        movieRes.setActor(movie.getActor());
        movieRes.setReleaseDate(movie.getReleaseDate());
        movieRes.setDesc(movie.getDesc());
        movieRes.setPoster(movie.getPoster());
        movieRes.setTrailerLink(movie.getTrailerLink());
        movieRes.setDuration(movie.getDuration());
        movieRes.setRating(String.valueOf(movie.getRating()));

        return movieRes;
    }

    public ResponseEntity<GenericResponse> adminGetAllMovie(String genresId, Pageable pageable) {
        Page<Movie> moviePage;
        if (genresId != null && !genresId.isBlank()) {
            Criteria criteria = Criteria.where("genres.id").is(genresId);
            Query query = new Query(criteria);

            List<Movie> movies = mongoTemplate.find(query, Movie.class).stream()
                    .sorted(Comparator.comparing(Movie::getMovieId, Comparator.reverseOrder())).toList();
            moviePage = PaginationUtils.paginate(movies, pageable);
        } else {
            moviePage = movieRepository.findAllByOrderByMovieIdDesc(pageable);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("content", moviePage.getContent());
        map.put("pageNumber", moviePage.getPageable().getPageNumber() + 1);
        map.put("pageSize", moviePage.getSize());
        map.put("totalPages", moviePage.getTotalPages());
        map.put("totalElements", moviePage.getTotalElements());

        return ResponseEntity.status(HttpStatus.OK)
                .body(GenericResponse.builder()
                        .success(true)
                        .message("Lấy tất cả phim thành công!")
                        .result(map)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    public ResponseEntity<GenericResponse> findMovieById(String id) {
        try {
            Optional<Movie> movie = movieRepository.findById(id);
            return movie.map(value -> ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy thông tin phim thành công!")
                            .result(value)
                            .statusCode(HttpStatus.OK.value())
                            .build())).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Phim không tìm thấy.")
                            .result(null)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .build()));

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }


    public ResponseEntity<GenericResponse> save(MovieReq req) {
        try {
            if (movieRepository.findByTitle(req.getTitle()).isEmpty()) {
                List<String> genresIds = req.getGenres();
                if (genresIds.contains(null))
                    throw new IllegalArgumentException("Danh sách thể loại không hợp lệ.");
                List<Genres> genres = genresRepository.findAllById(genresIds);
                Movie movie = new Movie(req.getTitle(), req.getDirector(), genres, req.getActor(), req.getReleaseDate(), req.getDesc(), req.getTrailerLink(), req.getDuration());

                String url = cloudinaryService.uploadImage(req.getPoster());
                movie.setPoster(url);

                String url2 = cloudinaryService.uploadImage(req.getSlider());
                movie.setSlider(url2);

                Movie movieRes = movieRepository.save(movie);
                BeanUtils.copyProperties(req, movie);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Thêm phim thành công!")
                                .result(movieRes)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Phim đã tồn tại.")
                                .result(null)
                                .statusCode(HttpStatus.CONFLICT.value())
                                .build());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> update(String movieId, UpdateMovieReq movieRequest) {
        try {
            Optional<Movie> optionalMovie = movieRepository.findById(movieId);
            if (optionalMovie.isPresent()){
                List<String> genresIds = movieRequest.getGenres();
                if (genresIds.contains(null))
                    throw new IllegalArgumentException("Danh sách thể loại không hợp lệ.");
                List<Genres> genres = genresRepository.findAllById(genresIds);
                Movie movie = optionalMovie.get();
                movie.setTitle(movieRequest.getTitle());
                movie.setDirector(movieRequest.getDirector());
                movie.setGenres(genres);
                movie.setActor(movieRequest.getActor());
                movie.setDesc(movieRequest.getDesc());
                movie.setReleaseDate(movieRequest.getReleaseDate());
                if (movieRequest.getPoster() != null) {
                    if (movie.getPoster() != null) {
                        cloudinaryService.deleteImage(movie.getPoster());
                    }
                    String url = cloudinaryService.uploadImage(movieRequest.getPoster());
                    movie.setPoster(url);
                }
                if (movieRequest.getSlider() != null) {
                    if (movie.getSlider() != null) {
                        cloudinaryService.deleteImage(movie.getSlider());
                    }
                    String url2 = cloudinaryService.uploadImage(movieRequest.getSlider());
                    movie.setSlider(url2);
                }
                movie.setTrailerLink(movieRequest.getTrailerLink());
                movie.setDuration(movieRequest.getDuration());

                Movie updateMovie = movieRepository.save(movie);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Cập nhật phim thành công!")
                                .result(updateMovie)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Phim không tìm thấy.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> updateIsDelete(String movieId) {
        try {
            Optional<Movie> optionalMovie = movieRepository.findById(movieId);
            if (optionalMovie.isPresent()){
                optionalMovie.get().setDelete(!optionalMovie.get().isDelete());
                movieRepository.save(optionalMovie.get());
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Cập nhật trạng thái phim thành công!")
                                .result(optionalMovie.get())
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Cập nhật trạng thái phim thất bai! Không tìm thấy phim.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> findNowPlayingMovies() {
        try {
            List<ShowTime> showTimes = showTimeRepository.findAllByStatusAndIsSpecialIsFalseAndIsDeleteIsFalse(ShowStatus.SHOWING);

            List<MovieViewRes> uniqueMovies = getUniqueMovies(showTimes);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy phim đang chiếu thành công!")
                            .result(uniqueMovies)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> findComingSoonMovies() {
        try {
            List<ShowTime> showTimes = showTimeRepository.findAllByStatusAndIsSpecialIsFalseAndIsDeleteIsFalse(ShowStatus.COMING_SOON);

            List<MovieViewRes> uniqueMovies = getUniqueMovies(showTimes);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy phim sắp chiếu thành công!")
                            .result(uniqueMovies)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> findSpecialMovies() {
        try {
            List<ShowTime> showTimes = showTimeRepository.findAllByIsSpecialIsTrueAndIsDeleteIsFalse().stream()
                    .filter(item -> !item.getStatus().equals(ShowStatus.ENDED)).toList();

            List<MovieViewRes> uniqueMovies = getUniqueMovies(showTimes);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy xuất chiếu đặc biệt thành công!")
                            .result(uniqueMovies)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> findNowPlayingMoviesByCinema(String id, Pageable pageable) {
        try {
            List<Room> rooms = roomRepository.findAllByCinema_CinemaId(id);
            Page<ShowTime> showTimes = showTimeRepository.findAllByRoomInAndStatusInAndIsDeleteIsFalse(rooms, List.of(ShowStatus.SHOWING, ShowStatus.COMING_SOON), pageable);

            List<ShowScheduleResp> responses = new ArrayList<>();
            for (ShowTime showTime : showTimes.getContent()) {
                List<Schedule> schedules = scheduleRepository.findAllByShowTimeId(showTime.getShowTimeId())
                        .stream().sorted(Comparator.comparing(Schedule::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                                .thenComparing(Schedule::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                        .collect(Collectors.toList());
                ShowScheduleResp response = new ShowScheduleResp(
                        showTime.getShowTimeId(),
                        showTime.getRoom(),
                        showTime.getMovie(),
                        showTime.getTimeStart(),
                        showTime.getTimeEnd(),
                        showTime.isSpecial(),
                        showTime.getStatus(),
                        showTime.isDelete(),
                        schedules);
                responses.add(response);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("content", responses);
            map.put("pageNumber", showTimes.getPageable().getPageNumber() + 1);
            map.put("pageSize", showTimes.getSize());
            map.put("totalPages", showTimes.getTotalPages());
            map.put("totalElements", showTimes.getTotalElements());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy tất cả lịch chiếu của rạp thành công!")
                            .result(map)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> searchMovie(String keyWord) {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("title").regex(".*" + keyWord + ".*", "i"));
            List<Movie> foundMovies = mongoTemplate.find(query, Movie.class);

            if (foundMovies.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Phim không tìm thấy.")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Tìm kiếm phim thành công!")
                                .result(foundMovies)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<?> getUpcomingMovies(String userId) {
        try {
            List<Booking> bookings = bookingRepository.findAllByUserIdAndIsPaymentIsTrue(userId);
            List<HistoryMovieRes> historyMovieRes = new ArrayList<>();
            for (Booking item : bookings) {
                Schedule schedule = item.getSeats().get(0).getSchedule();
                LocalDateTime localDateTime = LocalDateTime.of(schedule.getDate(), schedule.getStartTime());
                Date start = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                if (start.after(new Date())){
                    ShowTime showTime = item.getSeats().get(0).getShowTime();
                    HistoryMovieRes upcoming = new HistoryMovieRes();
                    upcoming.setBookingId(item.getBookingId());
                    upcoming.setMovieId(showTime.getMovie().getMovieId());
                    upcoming.setMovieName(showTime.getMovie().getTitle());
                    upcoming.setCinemaName(showTime.getRoom().getCinema().getCinemaName());
                    upcoming.setDate(schedule.getDate());
                    upcoming.setStartTime(schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                    upcoming.setPrice(item.getTotal());
                    upcoming.setCreateAt(item.getCreateAt());
                    upcoming.setTicketStatus(item.getTicketStatus());

                    historyMovieRes.add(upcoming);
                }
            }
            Collections.reverse(historyMovieRes);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy danh sách phim sắp chiếu thành công!")
                            .result(historyMovieRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<?> getViewedMovies(String userId) {
        try {
            List<Booking> bookings = bookingRepository.findAllByUserIdAndIsPaymentIsTrue(userId);
            List<HistoryMovieRes> historyMovieRes = new ArrayList<>();
            for (Booking item : bookings) {
                ShowTime showTime = item.getSeats().get(0).getShowTime();
                Schedule schedule = item.getSeats().get(0).getSchedule();
                LocalDateTime localDateTime = LocalDateTime.of(schedule.getDate(), schedule.getStartTime());
                Date end = Date.from(localDateTime.plusMinutes(Integer.parseInt(showTime.getMovie().getDuration())).atZone(ZoneId.systemDefault()).toInstant());
                if (end.before(new Date())){
                    HistoryMovieRes viewed = new HistoryMovieRes();
                    viewed.setBookingId(item.getBookingId());
                    viewed.setMovieId(showTime.getMovie().getMovieId());
                    viewed.setMovieName(showTime.getMovie().getTitle());
                    viewed.setCinemaName(showTime.getRoom().getCinema().getCinemaName());
                    viewed.setDate(schedule.getDate());
                    viewed.setStartTime(schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                    viewed.setPrice(item.getTotal());
                    viewed.setCreateAt(item.getCreateAt());
                    viewed.setTicketStatus(item.getTicketStatus());

                    historyMovieRes.add(viewed);
                }
            }
            Collections.reverse(historyMovieRes);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy danh sách phim đã xem thành công!")
                            .result(historyMovieRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> findTopMovie(int top) {
        try {
            List<Movie> movies = movieRepository.findByOrderByRatingDesc();
            List<String> movieN = new ArrayList<>();
            List<String> rating = new ArrayList<>();
            int dem = 0;
            for (Movie movie : movies) {
                movieN.add(movie.getTitle());
                rating.add(String.valueOf(movie.getRating()));
                dem++;
                if (dem == top) {
                    break;
                }
            }
            Map<String, Object> topMovies = new HashMap<>();
            topMovies.put("data", rating);
            topMovies.put("movie", movieN);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Top " + top + " phim đánh giá cao nhất!")
                            .result(topMovies)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> findNowPlayingMoviesAndSpecialByStaff(String staffId) {
        try {
            Optional<Staff> staff = staffRepository.findById(staffId);
            if (staff.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Không tìm thấy nhân viên!")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }

            List<Room> rooms = roomRepository.findAllByCinema(staff.get().getCinema());
            List<ShowTime> showTimes = showTimeRepository.findAllByStatusAndRoomInAndIsSpecialIsFalseAndIsDeleteIsFalse(ShowStatus.SHOWING, rooms);
            List<ShowTime> showTimes1 = showTimeRepository.findAllByRoomInAndIsSpecialIsTrueAndIsDeleteIsFalse(rooms);
            showTimes.addAll(showTimes1.stream().filter(item -> !item.getStatus().equals(ShowStatus.ENDED)).toList());

            List<MovieViewRes> uniqueMovies = getUniqueMovies(showTimes);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy phim đang chiếu và xuất chiếu đặc biệt thành công!")
                            .result(uniqueMovies)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> findComingSoonMoviesByStaff(String staffId) {
        try {
            Optional<Staff> staff = staffRepository.findById(staffId);
            if (staff.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Không tìm thấy nhân viên!")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            List<Room> rooms = roomRepository.findAllByCinema(staff.get().getCinema());
            List<ShowTime> showTimes = showTimeRepository.findAllByStatusAndRoomInAndIsSpecialIsFalseAndIsDeleteIsFalse(ShowStatus.COMING_SOON, rooms);

            List<MovieViewRes> uniqueMovies = getUniqueMovies(showTimes);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy phim sắp chiếu thành công!")
                            .result(uniqueMovies)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private List<MovieViewRes> getUniqueMovies(List<ShowTime> showTimes) {
        Map<String, MovieViewRes> uniqueMoviesMap = new HashMap<>();

        for (ShowTime showTime : showTimes) {
            if (!uniqueMoviesMap.containsKey(showTime.getMovie().getMovieId())) {
                uniqueMoviesMap.put(showTime.getMovie().getMovieId(), new MovieViewRes(
                        showTime.getMovie().getMovieId(),
                        showTime.getMovie().getTitle(),
                        showTime.getMovie().getPoster(),
                        showTime.getMovie().getSlider(),
                        showTime.getMovie().getRating(),
                        showTime.getMovie().getDirector(),
                        showTime.getMovie().getGenres(),
                        showTime.getMovie().getActor(),
                        showTime.getMovie().getDesc(),
                        showTime.getMovie().getTrailerLink(),
                        showTime.getMovie().getDuration()));
            }
        }
        return new ArrayList<>(uniqueMoviesMap.values());
    }
}
