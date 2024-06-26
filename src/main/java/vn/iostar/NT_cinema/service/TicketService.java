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
                            .message("Get total tickets success")
                            .result(count)
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
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
                            .message("Get total tickets success")
                            .result(res)
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getTotalTicketsByCinema(String id) {
        try {
            Optional<Cinema> cinema = cinemaRepository.findById(id);
            if (cinema.isEmpty()){
                return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                        .success(false)
                        .message("Cinema not found")
                        .result(null)
                        .statusCode(HttpStatus.OK.value())
                        .build());
            }
            Long count = ticketRepository.countByCinemaName(cinema.get().getCinemaName());
            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Get total tickets by cinema success")
                            .result(count)
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getTicketsSoldBetweenDates(Date startDate, Date endDate) {
        try {
            List<Ticket> tickets = ticketRepository.findTicketsSoldBetweenDates(startDate, endDate);
            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Get total tickets of time success")
                            .result(tickets.size())
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getTotalTicketsByCinemaOfManager(String managerId) {
        try {
            Optional<Manager> manager = userRepository.findById(managerId);
            if (manager.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        GenericResponse.builder()
                                .success(false)
                                .message("Manager not found")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            Long count = ticketRepository.countByCinemaName(manager.get().getCinema().getCinemaName());
            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Get total tickets by cinema of manager success")
                            .result(count)
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getTicketsSoldBetweenDatesOfManager(Date startDate, Date endDate, String managerId) {
        try {
            Optional<Manager> manager = userRepository.findById(managerId);
            if (manager.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Manager not have cinema")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            List<Ticket> tickets = ticketRepository.findTicketsSoldBetweenDatesByManager(startDate, endDate, manager.get().getCinema().getCinemaName());
            Map<String, Object> map = new HashMap<>();
            map.put("name", manager.get().getCinema().getCinemaName());
            map.put("totalTicket", tickets.size());
            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Get total tickets of time success")
                            .result(map)
                            .statusCode(200)
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
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
                            .message("Get all ticket success")
                            .result(map)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }
}
