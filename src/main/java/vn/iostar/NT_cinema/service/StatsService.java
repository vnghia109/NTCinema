package vn.iostar.NT_cinema.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.Finance;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

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

    public ResponseEntity<GenericResponse> getRevenueStats(Integer year, Integer month, boolean isTicket) {
        try {
            List<Map<String, Object>> cinemaStats = new ArrayList<>();
            List<Cinema> cinemas = cinemaRepository.findAll();

            if (year != null && month == null) {
                // Thống kê theo năm
                for (Cinema cinema : cinemas) {
                    getRevenueByYear(year, cinemaStats, cinema, isTicket);
                }
            }
            if (year != null && month != null) {
                // Thống kê theo tháng
                for (Cinema cinema : cinemas) {
                    getRevenueByMonth(year, month, cinemaStats, cinema, isTicket);
                }
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Thống kê thành công!!")
                            .result(cinemaStats)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void getRevenueByMonth(Integer year, Integer month, List<Map<String, Object>> cinemaStats, Cinema cinema, boolean isTicket) {
        LocalDate month1 = LocalDate.of(year, month, 1);
        List<BigDecimal> monthlyRevenues = new ArrayList<>(Collections.nCopies(month1.lengthOfMonth(), BigDecimal.ZERO));
        LocalDate start = LocalDate.of(month1.getYear(), month1.getMonthValue(), 1);
        LocalDate end = LocalDate.of(month1.getYear(), month1.getMonthValue(), month1.lengthOfMonth());
        List<DailyStats> dailyStats = dailyStatsRepository.findByCinemaAndDateBetween(cinema, start, end);
        Map<String, Object> monthlyStats = new HashMap<>();
        if (isTicket) {
            int totalTicket = 0;
            for (DailyStats dailyStat : dailyStats) {
                totalTicket = totalTicket + dailyStat.getTotalOfTickets();
            }

            monthlyStats.put("name", cinema.getCinemaName());
            monthlyStats.put("totalTicket", totalTicket);
        }else {
            for (DailyStats dailyStat : dailyStats) {
                monthlyRevenues.set(dailyStat.getDate().getDayOfMonth() - 1, dailyStat.getRevenue());
            }
            monthlyStats.put("name", cinema.getCinemaName());
            monthlyStats.put("data", monthlyRevenues);
        }
        cinemaStats.add(monthlyStats);
    }

    public void getRevenueByYear(Integer year, List<Map<String, Object>> cinemaStats, Cinema cinema, boolean isTicket) {
        List<MonthlyStats> monthlyStats = monthlyStatsRepository.findByCinemaAndMonthBetween(cinema,
                LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)
        );
        Map<String, Object> revenueByYearAndCinema = new HashMap<>();
        if (isTicket) {
            int totalTicket = 0;
            for (MonthlyStats monthlyStat : monthlyStats) {
                totalTicket = totalTicket + monthlyStat.getTotalOfTickets();
            }
            revenueByYearAndCinema.put("name", cinema.getCinemaName());
            revenueByYearAndCinema.put("totalTicket", totalTicket);
        }else {
            List<BigDecimal> revenueByMonth = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));

            for (MonthlyStats monthlyStat : monthlyStats) {
                revenueByMonth.set(monthlyStat.getMonth().getMonth().getValue() - 1, monthlyStat.getRevenue());
            }
            revenueByYearAndCinema.put("name", cinema.getCinemaName());
            revenueByYearAndCinema.put("data", revenueByMonth);
        }
        cinemaStats.add(revenueByYearAndCinema);
    }

    public ResponseEntity<GenericResponse> getRevenueStatsForManager(String managerId, Integer year, Integer month, boolean isTicket) {
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
            if (year != null && month == null) {
                // Thống kê theo năm
                getRevenueByYear(year, cinemaStats, cinema, isTicket);
            }
            if (year != null && month != null) {
                // Thống kê theo tháng
                getRevenueByMonth(year, month, cinemaStats, cinema, isTicket);

            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Thống kê thành công!!")
                            .result(cinemaStats)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
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

            List<User> users = userRepository.findAll();

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
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> getTopCinemasRevenue() {
        try {
            List<MonthlyStats> monthlyStats = monthlyStatsRepository.findAll();
            Map<String, BigDecimal> map = new HashMap<>();
            for (MonthlyStats monthlyStat : monthlyStats) {
                map.merge(
                        monthlyStat.getCinema().getCinemaName(),
                        monthlyStat.getRevenue(),
                        BigDecimal::add
                );
            }
            Map<String, Object> result = new HashMap<>();
            result.put("cinemaNames", map.keySet());
            result.put("revenue", map.values());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Xếp hạng doanh thu các rạp!")
                            .result(result)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> getTopUsers(int top, boolean isStaff) {
        try {
            List<BigDecimal> money = new ArrayList<>();
            List<Integer> ticket = new ArrayList<>();
            List<String> name = new ArrayList<>();

            if (isStaff) {
                List<StaffStats> staffStats = staffStatsRepository.findAllByOrderByTotalOfTicketsDesc().stream()
                        .sorted(Comparator.comparing(StaffStats::getRevenue, Comparator.nullsLast(Comparator.naturalOrder())).reversed()).toList();
                int dem = 0;
                for (StaffStats staffStat : staffStats) {
                    money.add(staffStat.getRevenue());
                    ticket.add(staffStat.getTotalOfTickets());
                    name.add(staffStat.getStaff().getFullName());

                    dem++;
                    if (dem == top) {
                        break;
                    }
                }

                Map<String, Object> topUsers = new HashMap<>();
                topUsers.put("money", money);
                topUsers.put("ticket", ticket);
                topUsers.put("name", name);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Top " + top +" nhân viên đóng góp nhiều nhất!")
                                .result(topUsers)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }else {
                List<UserStats> userStats = userStatsRepository.findAllByOrderByTotalOfTicketsDesc().stream()
                        .sorted(Comparator.comparing(UserStats::getTotalSpent, Comparator.nullsLast(Comparator.naturalOrder())).reversed()).toList();
                int dem = 0;
                for (UserStats userStat : userStats) {
                    money.add(userStat.getTotalSpent());
                    ticket.add(userStat.getTotalOfTickets());
                    name.add(userStat.getUser().getFullName());
                    dem++;
                    if (dem == top) {
                        break;
                    }
                }

                Map<String, Object> topUsers = new HashMap<>();
                topUsers.put("money", money);
                topUsers.put("ticket", ticket);
                topUsers.put("name", name);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Top " + top +" người dùng thân thiết nhất!")
                                .result(topUsers)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
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
            List<Staff> staffs = staffRepository.findAllByRoleAndCinema(roleRepository.findByRoleName("STAFF"), manager.get().getCinema());

            List<BigDecimal> money = new ArrayList<>();
            List<Integer> ticket = new ArrayList<>();
            List<String> name = new ArrayList<>();
            if (isStaff) {
                List<StaffStats> staffStats = staffStatsRepository.findAllByStaffIn(staffs).stream()
                        .sorted(Comparator.comparing(StaffStats::getTotalOfTickets, Comparator.nullsLast(Comparator.naturalOrder())).reversed()
                                .thenComparing(StaffStats::getRevenue, Comparator.nullsLast(Comparator.naturalOrder())).reversed()).toList();
                int dem = 0;
                for (StaffStats staffStat : staffStats) {
                    money.add(staffStat.getRevenue());
                    ticket.add(staffStat.getTotalOfTickets());
                    name.add(staffStat.getStaff().getFullName());
                    dem++;
                    if (dem == top) {
                        break;
                    }
                }
                Map<String, Object> topUsers = new HashMap<>();
                topUsers.put("money", money);
                topUsers.put("ticket", ticket);
                topUsers.put("name", name);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Top " + top +" nhân viên đóng góp nhiều nhất!")
                                .result(topUsers)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }else {
                List<UserStats> userStats = userStatsRepository.findAllByOrderByTotalOfTicketsDesc().stream()
                        .sorted(Comparator.comparing(UserStats::getTotalSpent, Comparator.nullsLast(Comparator.naturalOrder())).reversed()).toList();
                int dem = 0;
                for (UserStats userStat : userStats) {
                    money.add(userStat.getTotalSpent());
                    ticket.add(userStat.getTotalOfTickets());
                    name.add(userStat.getUser().getFullName());
                    dem++;
                    if (dem == top) {
                        break;
                    }
                }

                Map<String, Object> topUsers = new HashMap<>();
                topUsers.put("money", money);
                topUsers.put("ticket", ticket);
                topUsers.put("name", name);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(GenericResponse.builder()
                                .success(true)
                                .message("Top " + top +" người dùng thân thiết nhất!")
                                .result(topUsers)
                                .statusCode(HttpStatus.OK.value())
                                .build());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
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
                item.put("cinema", cinema.getCinemaId());
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
            throw new RuntimeException(e.getMessage());
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
            item.put("cinema", manager.get().getCinema().getCinemaId());
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
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> getFinanceDetail(String cinemaId, Integer year) {
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
            List<CinemaFinanceStats> statsList = cinemaFinanceStatsRepository.findAllByCinemaAndMonthBetween(cinema.get(),
                    LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
            Finance result = getFinanceResult(statsList);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy chi tiết thống kê thu chi thành công!")
                            .result(result)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @NotNull
    private static Finance getFinanceResult(List<CinemaFinanceStats> statsList) {
        Finance result = new Finance();
        for (CinemaFinanceStats stats : statsList) {
            result.setTotalRevenue(result.getTotalRevenue().add(stats.getTotalRevenue()));
            result.setTicketRevenue(result.getTicketRevenue().add(stats.getTicketRevenue()));
            result.setFoodRevenue(result.getFoodRevenue().add(stats.getFoodRevenue()));
            result.setTotalExpense(result.getTotalExpense().add(stats.getTotalExpense()));
            result.setFoodExpense(result.getFoodExpense().add(stats.getFoodExpense()));
            result.setOtherExpense(result.getOtherExpense().add(stats.getOtherExpense()));
            result.setTotalOfBooking(result.getTotalOfBooking() + stats.getTotalOfBooking());
            result.setTotalOfOrder(result.getTotalOfOrder() + stats.getTotalOfOrder());
            result.setProfit(result.getProfit().add(stats.getProfit()));
        }
        return result;
    }
}
