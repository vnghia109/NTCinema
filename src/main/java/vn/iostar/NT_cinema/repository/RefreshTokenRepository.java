package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.RefreshToken;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    List<RefreshToken> findAllByUser_UserIdAndExpiredIsFalseAndRevokedIsFalse(String userId);

    Optional<RefreshToken> findByTokenAndExpiredIsFalseAndRevokedIsFalse(String refreshToken);

    Optional<RefreshToken> findByUser_UserIdAndExpiredIsFalseAndRevokedIsFalse(String userId);

    void deleteAllByExpiredIsTrueAndRevokedIsTrue();
}
