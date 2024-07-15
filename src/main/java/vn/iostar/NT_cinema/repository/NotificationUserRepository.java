package vn.iostar.NT_cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.NotificationUser;

import java.util.List;

@Repository
public interface NotificationUserRepository extends MongoRepository<NotificationUser, String> {
    Page<NotificationUser> findAllByUser_UserIdOrderByNotificationUserIdDesc(String userId, Pageable pageable);
    List<NotificationUser> findAllByUser_UserIdAndReadIsFalse(String userId);

    long countAllByUser_UserIdAndReadIsFalse(String userId);
}
