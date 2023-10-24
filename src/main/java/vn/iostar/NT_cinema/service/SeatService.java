package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.SeatReq;
import vn.iostar.NT_cinema.entity.Seat;
import vn.iostar.NT_cinema.repository.PriceRepository;
import vn.iostar.NT_cinema.repository.SeatRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeatService {
    @Autowired
    SeatRepository seatRepository;

    @Autowired
    PriceRepository priceRepository;

    public void addSeat(String showtimeId, List<SeatReq> seatReq){
        List<Seat> seats = new ArrayList<>();

        for (SeatReq item : seatReq) {
            Seat seat = new Seat();
            seat.setShowTimeId(showtimeId);
            seat.setPrice(priceRepository.findByType(item.getPriceType()).get());
            seat.setColumn(item.getColumn());
            seat.setRow(item.getRow());
            seat.setStatus(true);

            seats.add(seat);
        }

        seatRepository.saveAll(seats);
    }
}
