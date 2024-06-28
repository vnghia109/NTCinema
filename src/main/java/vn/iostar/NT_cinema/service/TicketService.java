package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.CinemaTicketRes;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.entity.Cinema;
import vn.iostar.NT_cinema.entity.Manager;
import vn.iostar.NT_cinema.entity.Ticket;
import vn.iostar.NT_cinema.exception.NotFoundException;
import vn.iostar.NT_cinema.repository.CinemaRepository;
import vn.iostar.NT_cinema.repository.ManagerRepository;
import vn.iostar.NT_cinema.repository.TicketRepository;

import java.util.*;

@Service
public class TicketService {
    @Autowired
    TicketRepository ticketRepository;
    @Autowired
    ManagerRepository userRepository;
    @Autowired
    CinemaRepository cinemaRepository;

    public ResponseEntity<GenericResponse> getTotalTickets() {
        try {
            Long count = ticketRepository.count();
            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("lấy tổng số vé thành công!")
                            .result(count)
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> getTotalTicketsByYear(int year) {
        try {
            List<Cinema> cinemas = cinemaRepository.findAll();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            Date startDate = calendar.getTime();

            calendar.set(Calendar.MONTH, Calendar.DECEMBER);
            calendar.set(Calendar.DAY_OF_MONTH, 31);
            Date endDate = calendar.getTime();

            List<CinemaTicketRes> res = new ArrayList<>();
            for (Cinema item : cinemas) {
                List<Ticket> tickets = ticketRepository.findTicketsByDateAndCinemaName(startDate, endDate, item.getCinemaName());
                CinemaTicketRes temp = new CinemaTicketRes(item.getCinemaName(), tickets.size());
                res.add(temp);
            }
            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Lấy tổng số vé thành công!")
                            .result(res)
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> getTotalTicketsByCinema(String id) {
        try {
            Optional<Cinema> cinema = cinemaRepository.findById(id);
            if (cinema.isEmpty()){
                throw new NotFoundException("Rạp phim không tồn tại!");
            }
            Long count = ticketRepository.countByCinemaName(cinema.get().getCinemaName());
            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Lấy tổng vé theo rạp phim thành công!")
                            .result(count)
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> getTicketsSoldBetweenDates(Date startDate, Date endDate) {
        try {
            List<Ticket> tickets = ticketRepository.findTicketsSoldBetweenDates(startDate, endDate);
            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Lấy tổng số vé thành công!")
                            .result(tickets.size())
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> getTotalTicketsByCinemaOfManager(String managerId) {
        try {
            Optional<Manager> manager = userRepository.findById(managerId);
            if (manager.isEmpty())
                throw new NotFoundException("Quản lý không tồn tại. Vui lòng đăng nhập lại.");

            Long count = ticketRepository.countByCinemaName(manager.get().getCinema().getCinemaName());
            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Lấy tổng số vé thành công!")
                            .result(count)
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> getTicketsSoldBetweenDatesOfManager(Date startDate, Date endDate, String managerId) {
        try {
            Optional<Manager> manager = userRepository.findById(managerId);
            if (manager.isEmpty())
                throw new NotFoundException("Quản lý không tồn tại. Vui lòng đăng nhập lại.");

            List<Ticket> tickets = ticketRepository.findTicketsSoldBetweenDatesByManager(startDate, endDate, manager.get().getCinema().getCinemaName());
            Map<String, Object> map = new HashMap<>();
            map.put("name", manager.get().getCinema().getCinemaName());
            map.put("totalTicket", tickets.size());
            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Lấy tất cả các vé thành công!")
                            .result(map)
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public ResponseEntity<GenericResponse> getTickets(Pageable pageable){
        try {
            Page<Ticket> tickets = ticketRepository.findAll(pageable);

            Map<String, Object> map = new HashMap<>();
            map.put("content", tickets.getContent());
            map.put("pageNumber", tickets.getPageable().getPageNumber() + 1);
            map.put("pageSize", tickets.getSize());
            map.put("totalPages", tickets.getTotalPages());
            map.put("totalElements", tickets.getTotalElements());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy tất cả các vé thành công!")
                            .result(map)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
