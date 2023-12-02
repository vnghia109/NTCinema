package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;
import vn.iostar.NT_cinema.entity.Booking;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
}
