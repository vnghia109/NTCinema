package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.entity.Manager;
import vn.iostar.NT_cinema.entity.Ticket;
import vn.iostar.NT_cinema.entity.User;
import vn.iostar.NT_cinema.repository.ManagerRepository;
import vn.iostar.NT_cinema.repository.TicketRepository;
import vn.iostar.NT_cinema.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {
    @Autowired
    TicketRepository ticketRepository;
    @Autowired
    ManagerRepository userRepository;

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

    public ResponseEntity<GenericResponse> getTotalTicketsByCinema(String cinemaName) {
        try {
            Long count = ticketRepository.countByCinemaName(cinemaName);
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
}
