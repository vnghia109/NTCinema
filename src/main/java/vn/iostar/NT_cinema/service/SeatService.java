package vn.iostar.NT_cinema.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.PriceType;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.SeatBookedRes;
import vn.iostar.NT_cinema.dto.SeatReq;
import vn.iostar.NT_cinema.entity.Price;
import vn.iostar.NT_cinema.entity.Schedule;
import vn.iostar.NT_cinema.entity.Seat;
import vn.iostar.NT_cinema.entity.ShowTime;
import vn.iostar.NT_cinema.exception.NotFoundException;
import vn.iostar.NT_cinema.repository.PriceRepository;
import vn.iostar.NT_cinema.repository.ScheduleRepository;
import vn.iostar.NT_cinema.repository.SeatRepository;
import vn.iostar.NT_cinema.repository.ShowTimeRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatService {
    @Autowired
    SeatRepository seatRepository;

    @Autowired
    PriceRepository priceRepository;

    @Autowired
    ShowTimeRepository showTimeRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    BookingService bookingService;

    public ResponseEntity<GenericResponse> checkSeat(String showtimeId, List<SeatReq> seatReq){
        try {
            Optional<ShowTime> showTime = showTimeRepository.findById(showtimeId);
            if (showTime.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Lịch chiếu không tồn tại")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            List<Seat> seats = new ArrayList<>();
            List<String> seatIds = new ArrayList<>();
            for (SeatReq item : seatReq) {
                Optional<Schedule> schedule = scheduleRepository.findById(item.getScheduleId());
                if (schedule.isEmpty())
                    throw new NotFoundException("Giờ chiếu không tồn tại.");
                Optional<Seat> optionalSeat = seatRepository.findByColumnAndRowAndShowTimeAndScheduleAndStatusIsTrue(item.getColumn(), item.getRow(), showTime.get(), schedule.get());
                if (optionalSeat.isPresent()){
                    seatIds.add(optionalSeat.get().getSeatId());
                    continue;
                }
                Optional<Price> price = priceRepository.findByType(PriceType.valueOf(item.getPriceType()));
                if (price.isEmpty()){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(GenericResponse.builder()
                                    .success(false)
                                    .message("Loại ghế không tồn tại.")
                                    .result(null)
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build());
                }
                Seat seat = new Seat();
                seat.setShowTime(showTime.get());
                seat.setPrice(price.get());
                seat.setColumn(item.getColumn());
                seat.setRow(item.getRow());
                seat.setStatus(true);
                seat.setSchedule(schedule.get());

                seats.add(seat);
            }
            List<Seat> seatList = seatRepository.saveAll(seats);
            seatIds.addAll(seatList.stream()
                    .map(Seat::getSeatId)
                    .toList());
            Map<Object, List<String>> map = new HashMap<>();
            map.put("seatIds", seatIds);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Success")
                            .result(map)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }

    }

    public ResponseEntity<GenericResponse> getSeatBooked(String showtimeId, String scheduleId){
        try {
            Optional<ShowTime> showTime = showTimeRepository.findById(showtimeId);
            if (showTime.isEmpty())
                throw new NotFoundException("Lịch chiếu không tồn tại.");
            Optional<Schedule> schedule = scheduleRepository.findById(scheduleId);
            if (schedule.isEmpty())
                throw new NotFoundException("Giờ chiếu không tồn tại.");
            List<Seat> seats = seatRepository.findAllByShowTimeAndScheduleAndStatusIsFalse(showTime.get(), schedule.get());
            List<SeatBookedRes> seatBookedRes = seats.stream()
                    .map(item -> new SeatBookedRes(item.getRow(), item.getColumn()))
                    .collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Get all seat booked success")
                            .result(seatBookedRes)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> countSeatBooked(String showtimeId, String scheduleId){
        try {
            Optional<ShowTime> showTime = showTimeRepository.findById(showtimeId);
            if (showTime.isEmpty())
                throw new NotFoundException("Lịch chiếu không tồn tại.");
            Optional<Schedule> schedule = scheduleRepository.findById(scheduleId);
            if (schedule.isEmpty())
                throw new NotFoundException("Giờ chiếu không tồn tại.");
            List<Seat> seats = seatRepository.findAllByShowTimeAndScheduleAndStatusIsFalse(showTime.get(), schedule.get());
            int SeatAvailable = showTime.get().getRoom().getColSeat()*showTime.get().getRoom().getRowSeat() - seats.size();

            Map<Object, Object> map = new HashMap<>();
            map.put("SeatBooked", seats.size());
            map.put("SeatAvailable", SeatAvailable);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Get all seat booked success")
                            .result(map)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    @Scheduled(fixedDelay = 6000)
    @PostConstruct
    public void resetSeatStatus(){
        List<Seat> seats = seatRepository.findAllByStatusIsFalse();
        for (Seat seat : seats) {
            if (bookingService.findBySeat(seat).isEmpty()){
                seat.setStatus(true);
                seatRepository.save(seat);
            }
        }
    }
}
