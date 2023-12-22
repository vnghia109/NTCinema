package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Booking;

import java.util.Date;
import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findAllByIsPaymentIsFalse();
    List<Booking> findAllByUserIdAndIsPaymentIsTrue(String userId);
    @Query("{'createAt': {'$gte': ?0, '$lt': ?1}, 'isPayment': true}")
    List<Booking> findAllPaidBookingsInDateRange(Date startDate, Date endDate);
    List<Booking> findAllByShowtimeIdIn(List<String> showtimeIds);
}
