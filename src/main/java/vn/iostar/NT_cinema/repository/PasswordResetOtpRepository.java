package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.PasswordResetOtp;
import vn.iostar.NT_cinema.entity.User;

import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends MongoRepository<PasswordResetOtp, String> {
    Optional<PasswordResetOtp> findByUser(User user);

    Optional<PasswordResetOtp> findByOtp(String otp);
}
