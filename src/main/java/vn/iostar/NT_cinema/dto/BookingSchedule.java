package vn.iostar.NT_cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.iostar.NT_cinema.entity.Booking;
import vn.iostar.NT_cinema.entity.Schedule;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BookingSchedule {
    private Booking booking;
    private Schedule schedule;

    public BookingSchedule(Booking booking) {
        this.booking = booking;
        this.schedule = booking.getSeats().get(0).getSchedule();
    }
}
