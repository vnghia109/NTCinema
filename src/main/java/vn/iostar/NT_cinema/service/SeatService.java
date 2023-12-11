package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.constant.PriceType;
import vn.iostar.NT_cinema.dto.BookedSeatReq;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.SeatBookedRes;
import vn.iostar.NT_cinema.dto.SeatReq;
import vn.iostar.NT_cinema.entity.Price;
import vn.iostar.NT_cinema.entity.Seat;
import vn.iostar.NT_cinema.repository.PriceRepository;
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

    public ResponseEntity<GenericResponse> checkSeat(String showtimeId, List<SeatReq> seatReq){
        try {
            if (showTimeRepository.findById(showtimeId).isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Showtime does not exist")
                                .result(null)
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .build());
            }
            List<Seat> seats = new ArrayList<>();
            List<String> seatIds = new ArrayList<>();
            for (SeatReq item : seatReq) {
                Optional<Seat> optionalSeat = seatRepository.findByColumnAndRowAndShowTimeIdAndTimeShowAndStatusIsTrue(item.getColumn(), item.getRow(), showtimeId, item.getTimeShow());
                if (optionalSeat.isPresent()){
                    seatIds.add(optionalSeat.get().getSeatId());
                    continue;
                }
                Optional<Price> price = priceRepository.findByType(PriceType.valueOf(item.getPriceType()));
                if (price.isEmpty()){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(GenericResponse.builder()
                                    .success(false)
                                    .message("Price type does not exist")
                                    .result(null)
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build());
                }
                Seat seat = new Seat();
                seat.setShowTimeId(showtimeId);
                seat.setPrice(price.get());
                seat.setColumn(item.getColumn());
                seat.setRow(item.getRow());
                seat.setStatus(true);
                seat.setTimeShow(item.getTimeShow());

                seats.add(seat);
            }
            List<Seat> seatList = seatRepository.saveAll(seats);
            seatIds.addAll(seatList.stream()
                    .map(Seat::getSeatId)
                    .toList());
            Map<Object, List<String>> map = new HashMap<>();
            map.put("List seatId", seatIds);

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

    public ResponseEntity<GenericResponse> getSeatBooked(BookedSeatReq req) {
        try {
            List<Seat> seats = seatRepository.findAllByShowTimeIdAndTimeShowAndStatusIsFalse(req.getShowtimeId(), req.getTimeShow());
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
}
