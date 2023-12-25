package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Ticket;

import java.util.Date;
import java.util.List;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {
    long countByCinemaName(String cinemaName);

    @Query("{'createAt': {'$gte': ?0, '$lt': ?1}}")
    List<Ticket> findTicketsSoldBetweenDates(Date startDate, Date endDate);

    @Query("{'createAt': {'$gte': ?0, '$lt': ?1}, 'cinemaName': {$eq: ?2}}")
    List<Ticket> findTicketsSoldBetweenDatesByManager(Date startDate, Date endDate, String cinemaName);

    @Query("{ 'createAt': { $gte: ?0, $lt: ?1 }, 'cinemaName': ?2 }")
    List<Ticket> findTicketsByDateAndCinemaName(Date startDate, Date endDate, String cinemaName);
}
