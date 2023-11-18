package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.SeatReq;
import vn.iostar.NT_cinema.entity.Seat;
import vn.iostar.NT_cinema.repository.PriceRepository;
import vn.iostar.NT_cinema.repository.SeatRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatService {
    @Autowired
    SeatRepository seatRepository;

    @Autowired
    PriceRepository priceRepository;

    public ResponseEntity<GenericResponse> checkSeat(String showtimeId, List<SeatReq> seatReq){
        try {
            List<Seat> seats = new ArrayList<>();
            List<String> seatIds = new ArrayList<>();
            for (SeatReq item : seatReq) {
                Optional<Seat> optionalSeat = seatRepository.findByColumnAndRowAndShowTimeIdAndStatusIsTrue(item.getColumn(), item.getRow(), showtimeId);
                if (optionalSeat.isPresent()){
                    seatIds.add(optionalSeat.get().getSeatId());
                    continue;
                }
                Seat seat = new Seat();
                seat.setShowTimeId(showtimeId);
                seat.setPrice(priceRepository.findByType(item.getPriceType()).get());
                seat.setColumn(item.getColumn());
                seat.setRow(item.getRow());
                seat.setStatus(true);

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
                            .result("Internal Server Error")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }

    }
}
