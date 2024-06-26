package vn.iostar.NT_cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.StockEntry;
import vn.iostar.NT_cinema.entity.User;

@Repository
public interface StockEntryRepository extends MongoRepository<StockEntry, String> {
    Page<StockEntry> findAllByManager(User manager, Pageable pageable);
}
