package vn.iostar.NT_cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.expression.spel.ast.OpAnd;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.constant.TicketStatus;
import vn.iostar.NT_cinema.entity.Booking;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findAllByIsPaymentIsFalse();
    List<Booking> findAllByIsPaymentIsTrue();
    List<Booking> findAllByUserIdAndIsPaymentIsTrue(String userId);
    @Query("{'createAt': {'$gte': ?0, '$lt': ?1}, 'isPayment': true}")
    List<Booking> findAllPaidBookingsInDateRange(Date startDate, Date endDate);
    List<Booking> findAllByShowtimeIdIn(List<String> showtimeIds);
    @Query("{ 'createAt': { $gte: ?0, $lt: ?1 }, 'showtimeId': { $in: ?2 } }")
    List<Booking> findByYearAndShowtimeIds(Date startDate, Date endDate, List<String> showtimeIds);
    Optional<Booking> findByBookingIdAndUserId(String bookingId, String userId);
    Page<Booking> findAllByTicketStatusOrderByBookingIdDesc(TicketStatus ticketStatus, Pageable pageable);
    Page<Booking> findAllByShowtimeIdInAndTicketStatusOrderByBookingIdDesc(List<String> showtimeIds, TicketStatus ticketStatus, Pageable pageable);
    Page<Booking> findAllByShowtimeIdInOrderByBookingIdDesc(List<String> showtimeIds, Pageable pageable);

    List<Booking> findAllByUserIdAndTicketStatus(String userId, TicketStatus ticketStatus);

    Page<Booking> findAllByOrderByBookingIdDesc(Pageable pageable);
}
