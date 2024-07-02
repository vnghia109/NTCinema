package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.constant.TicketStatus;
import vn.iostar.NT_cinema.entity.Booking;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findAllByIsPaymentIsFalse();
    List<Booking> findAllByIsPaymentIsTrue();
    List<Booking> findAllByUserIdAndIsPaymentIsTrue(String userId);
    Optional<Booking> findByBookingIdAndUserId(String bookingId, String userId);
    List<Booking> findAllByUserIdAndTicketStatus(String userId, TicketStatus ticketStatus);
}
