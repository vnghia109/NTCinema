package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.Finance;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.MovieViewRes;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {
    @Autowired
    DailyStatsRepository dailyStatsRepository;
    @Autowired
    MonthlyStatsRepository monthlyStatsRepository;
    @Autowired
    CinemaRepository cinemaRepository;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    ManagerRepository managerRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    ShowTimeRepository showTimeRepository;
    @Autowired
    StaffStatsRepository staffStatsRepository;
    @Autowired
    UserStatsRepository userStatsRepository;
    @Autowired
    CinemaFinanceStatsRepository cinemaFinanceStatsRepository;
    @Autowired
    StaffRepository staffRepository;

    public ResponseEntity<GenericResponse> getRevenueStats(Integer year, LocalDate month) {
        try {
            List<Map<String, Object>> cinemaStats = new ArrayList<>();
            List<Cinema> cinemas = cinemaRepository.findAll();

            if (year != null) {
                // Thống kê theo năm
                for (Cinema cinema : cinemas) {
                    List<MonthlyStats> monthlyStats = monthlyStatsRepository.findByCinemaAndMonthBetween(cinema,
                            LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)
                    );
                    List<BigDecimal> revenueByMonth = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));

                    for (MonthlyStats monthlyStat : monthlyStats) {
                        BigDecimal revenue = monthlyStat.getRevenue();

                        int month1 = monthlyStat.getMonth().getMonth().getValue();
                        revenueByMonth.set(month1 - 1, revenue);
                    }
                    Map<String, Object> revenueByYearAndCinema = new HashMap<>();
                    revenueByYearAndCinema.put("name", cinema.getCinemaName());
                    revenueByYearAndCinema.put("data", revenueByMonth);
                    cinemaStats.add(revenueByYearAndCinema);
                }
            } else if (month != null) {
                // Thống kê theo tháng
                for (Cinema cinema : cinemas){
                    List<BigDecimal> monthlyRevenues = new ArrayList<>(Collections.nCopies(month.lengthOfMonth(), BigDecimal.ZERO));
                    LocalDate start = LocalDate.of(month.getYear(), month.getMonthValue(), 1);
                    LocalDate end = LocalDate.of(month.getYear(), month.getMonthValue(), month.lengthOfMonth());
                    List<DailyStats> dailyStats = dailyStatsRepository.findByCinemaAndDateBetween(cinema, start, end);
                    for (DailyStats dailyStat : dailyStats) {
                        monthlyRevenues.set(dailyStat.getDate().getDayOfMonth() - 1, dailyStat.getRevenue());
                    }
                    Map<String, Object> monthlyStats = new HashMap<>();
                    monthlyStats.put("name", cinema.getCinemaName());
                    monthlyStats.put("data", monthlyRevenues);
                    cinemaStats.add(monthlyStats);
                }
            } else {
                throw new IllegalArgumentException("Bạn phải chọn tháng hoặc năm cần thống kê.");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("revenue", cinemaStats);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Thống kê thành công!!")
                            .result(result)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> getRevenueStatsForManager(String managerId, Integer year, LocalDate month) {
        try {
            Optional<Manager> manager = managerRepository.findById(managerId);
            if (manager.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Quản lý chưa được thêm rạp phim.")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            List<Map<String, Object>> cinemaStats = new ArrayList<>();

            Cinema cinema = manager.get().getCinema();
            if (year != null) {
                // Thống kê theo năm
                List<MonthlyStats> monthlyStats = monthlyStatsRepository.findByCinemaAndMonthBetween(cinema,
                        LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)
                );
                List<BigDecimal> revenueByMonth = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));

                for (MonthlyStats monthlyStat : monthlyStats) {
                    BigDecimal revenue = monthlyStat.getRevenue();

                    int month1 = monthlyStat.getMonth().getMonth().getValue();
                    revenueByMonth.set(month1 - 1, revenue);
                }
                Map<String, Object> revenueByYearAndCinema = new HashMap<>();
                revenueByYearAndCinema.put("name", cinema.getCinemaName());
                revenueByYearAndCinema.put("data", revenueByMonth);
                cinemaStats.add(revenueByYearAndCinema);
            } else if (month != null) {
                // Thống kê theo tháng
                List<BigDecimal> monthlyRevenues = new ArrayList<>(Collections.nCopies(month.lengthOfMonth(), BigDecimal.ZERO));
                LocalDate start = LocalDate.of(month.getYear(), month.getMonthValue(), 1);
                LocalDate end = LocalDate.of(month.getYear(), month.getMonthValue(), month.lengthOfMonth());
                List<DailyStats> dailyStats = dailyStatsRepository.findByCinemaAndDateBetween(cinema, start, end);
                for (DailyStats dailyStat : dailyStats) {
                    monthlyRevenues.set(dailyStat.getDate().getDayOfMonth() - 1, dailyStat.getRevenue());
                }
                Map<String, Object> monthlyStats = new HashMap<>();
                monthlyStats.put("name", cinema.getCinemaName());
                monthlyStats.put("data", monthlyRevenues);
                cinemaStats.add(monthlyStats);

            } else {
                throw new IllegalArgumentException("Bạn phải chọn tháng hoặc năm cần thống kê.");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("revenue", cinemaStats);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Thống kê thành công!!")
                            .result(result)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> getStatsOverview() {
        try {
            List<MonthlyStats> monthlyStats = monthlyStatsRepository.findAll();
            BigDecimal totalRevenue = monthlyStats.stream().map(MonthlyStats::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);

            LocalDate now = LocalDate.now().withDayOfMonth(1);
            Date start = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(now.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            List<ShowTime> showTimes = showTimeRepository.findByTimeStartBetween(start, end);
            Map<String, Movie> uniqueMoviesMap = new HashMap<>();
            for (ShowTime showTime : showTimes) {
                Movie movie = showTime.getMovie();
                String movieId = movie.getMovieId();

                if (!uniqueMoviesMap.containsKey(movieId)) {
                    uniqueMoviesMap.put(movieId, movie);
                }
            }
            List<Movie> movies = new ArrayList<>(uniqueMoviesMap.values());

            List<Role> roles = new ArrayList<>();
            roles.add(roleRepository.findByRoleName("VIEWER"));
            List<User> users = userRepository.findAllByRoleIn(roles);

            List<Cinema> cinemas = cinemaRepository.findAll();

            Map<String, Object> result = new HashMap<>();
            result.put("qRevenue", totalRevenue);
            result.put("qMovieOfMonth", movies.size());
            result.put("qCinema", cinemas.size());
            result.put("qUser", users.size());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy thống kê thành công!!")
                            .result(result)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> getTopUsers(int top, boolean isStaff) {
        try {
            List<Map<String, Object>> topUsers = new ArrayList<>();
            if (isStaff) {
                List<StaffStats> staffStats = staffStatsRepository.findAllByOrderByTotalOfTicketsDesc().stream()
                        .sorted(Comparator.comparing(StaffStats::getRevenue).reversed()).toList();
                int dem = 0;
                for (StaffStats staffStat : staffStats) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("money", staffStat.getRevenue());
                    result.put("ticket", staffStat.getTotalOfTickets());
                    result.put("name", staffStat.getStaff().getFullName());
                    topUsers.add(result);
                    dem++;
                    if (dem == top) {
                        break;
                    }
                }

                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Top " + top +" nhân viên đóng góp nhiều nhất!")
                                .result(topUsers)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }else {
                List<UserStats> userStats = userStatsRepository.findAllByOrderByTotalOfTicketsDesc().stream()
                        .sorted(Comparator.comparing(UserStats::getTotalSpent).reversed()).toList();
                int dem = 0;
                for (UserStats userStat : userStats) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("money", userStat.getTotalSpent());
                    result.put("ticket", userStat.getTotalOfTickets());
                    result.put("name", userStat.getUser().getFullName());
                    topUsers.add(result);
                    dem++;
                    if (dem == top) {
                        break;
                    }
                }
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Top " + top +" người dùng thân thiết nhất!")
                                .result(topUsers)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> getTopUsersOfManager(int top, boolean isStaff, String managerId) {
        try {
            Optional<Manager> manager = managerRepository.findById(managerId);
            if (manager.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Không tìm thấy quản lý. Vui lòng đăng nhập lại!")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            List<Staff> staffs = staffRepository.findAllByCinema(manager.get().getCinema());
            List<Map<String, Object>> topUsers = new ArrayList<>();
            if (isStaff) {
                List<StaffStats> staffStats = staffStatsRepository.findAllByStaffIn(staffs).stream()
                        .sorted(Comparator.comparing(StaffStats::getTotalOfTickets).reversed().thenComparing(StaffStats::getRevenue).reversed()).toList();
                int dem = 0;
                for (StaffStats staffStat : staffStats) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("money", staffStat.getRevenue());
                    result.put("ticket", staffStat.getTotalOfTickets());
                    result.put("name", staffStat.getStaff().getFullName());
                    topUsers.add(result);
                    dem++;
                    if (dem == top) {
                        break;
                    }
                }

                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Top " + top +" nhân viên đóng góp nhiều nhất!")
                                .result(topUsers)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }else {
                List<UserStats> userStats = userStatsRepository.findAllByOrderByTotalOfTicketsDesc().stream()
                        .sorted(Comparator.comparing(UserStats::getTotalSpent).reversed()).toList();
                int dem = 0;
                for (UserStats userStat : userStats) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("money", userStat.getTotalSpent());
                    result.put("ticket", userStat.getTotalOfTickets());
                    result.put("name", userStat.getUser().getFullName());
                    topUsers.add(result);
                    dem++;
                    if (dem == top) {
                        break;
                    }
                }
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Top " + top +" người dùng thân thiết nhất!")
                                .result(topUsers)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> getFinance(Integer year) {
        try {
            List<Cinema> cinemas = cinemaRepository.findAll();
            List<Map<String, Object>> result = new ArrayList<>();
            for (Cinema cinema : cinemas) {
                List<CinemaFinanceStats> cinemaFinanceStats = cinemaFinanceStatsRepository.findAllByCinemaAndMonthBetween(cinema,
                        LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));

                List<BigDecimal> revenue = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));
                List<BigDecimal> expense = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));
                for (CinemaFinanceStats financeStats : cinemaFinanceStats) {
                    revenue.set(financeStats.getMonth().getMonthValue() - 1, financeStats.getTotalRevenue());
                    expense.set(financeStats.getMonth().getMonthValue() - 1, financeStats.getTotalExpense());
                }

                Map<String, Object> item = new HashMap<>();
                item.put("name", cinema.getCinemaName());
                item.put("revenue", revenue);
                item.put("expense", expense);
                result.add(item);
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy danh sách thu chi thành công!")
                            .result(result)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> getFinanceOfManager(Integer year, String managerId) {
        try {
            Optional<Manager> manager = managerRepository.findById(managerId);
            if (manager.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Không tìm thấy quản lý. Vui này đăng nhập!")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            List<Map<String, Object>> result = new ArrayList<>();

            List<CinemaFinanceStats> cinemaFinanceStats = cinemaFinanceStatsRepository.findAllByCinemaAndMonthBetween(manager.get().getCinema(),
                    LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));

            List<BigDecimal> revenue = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));
            List<BigDecimal> expense = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));
            for (CinemaFinanceStats financeStats : cinemaFinanceStats) {
                revenue.set(financeStats.getMonth().getMonthValue() - 1, financeStats.getTotalRevenue());
                expense.set(financeStats.getMonth().getMonthValue() - 1, financeStats.getTotalExpense());
            }

            Map<String, Object> item = new HashMap<>();
            item.put("name", manager.get().getCinema().getCinemaName());
            item.put("revenue", revenue);
            item.put("expense", expense);
            result.add(item);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy danh sách thu chi thành công!")
                            .result(result)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> getFinanceDetail(String cinemaId, LocalDate month) {
        try {
            Optional<Cinema> cinema = cinemaRepository.findById(cinemaId);
            if (cinema.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Không tìm thấy rạp phim!")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            Optional<CinemaFinanceStats> stats = cinemaFinanceStatsRepository.findByCinemaAndMonth(cinema.get(), month.withDayOfMonth(1));
            Finance result = new Finance();
            if (stats.isEmpty()) {
                CinemaFinanceStats newStats = new CinemaFinanceStats(
                        month.withDayOfMonth(1), cinema.get());
                cinemaFinanceStatsRepository.save(newStats);
                result.setTotalRevenue(BigDecimal.ZERO);
                result.setTicketRevenue(BigDecimal.ZERO);
                result.setFoodRevenue(BigDecimal.ZERO);
                result.setTotalExpense(BigDecimal.ZERO);
                result.setFoodExpense(BigDecimal.ZERO);
                result.setOtherExpense(BigDecimal.ZERO);
                result.setTotalOfOrder(0);
                result.setProfit(BigDecimal.ZERO);
            }else {
                result.setTotalRevenue(stats.get().getTotalRevenue());
                BigDecimal ticketRevenue;
                if (stats.get().getTicketRevenue().equals(BigDecimal.ZERO) || stats.get().getTotalRevenue().equals(BigDecimal.ZERO)) {
                    ticketRevenue = BigDecimal.ZERO;
                }else {
                    ticketRevenue = stats.get().getTicketRevenue().divide(stats.get().getTotalRevenue(), 3, RoundingMode.HALF_UP);
                }
                result.setTicketRevenue(ticketRevenue);
                result.setFoodRevenue(ticketRevenue.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : BigDecimal.ONE.subtract(ticketRevenue));
                result.setTotalExpense(stats.get().getTotalExpense());
                BigDecimal foodExpense;
                if (stats.get().getFoodExpense().equals(BigDecimal.ZERO) || stats.get().getTotalExpense().equals(BigDecimal.ZERO)) {
                    foodExpense = BigDecimal.ZERO;
                }else {
                    foodExpense = stats.get().getFoodExpense().divide(stats.get().getTotalExpense(), 3, RoundingMode.HALF_UP);
                }
                result.setFoodExpense(foodExpense);
                result.setOtherExpense(foodExpense.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : BigDecimal.ONE.subtract(foodExpense));
                result.setTotalOfOrder(stats.get().getTotalOfOrder());
                result.setProfit(stats.get().getProfit());
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy chi tiết thống kê thu chi thành công!")
                            .result(result)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
}
