package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.UserTokenFCM;

import java.util.Optional;

@Repository
public interface UserTokenRepository extends MongoRepository<UserTokenFCM, String> {
    Optional<UserTokenFCM> findByUserId(String userId);
}
