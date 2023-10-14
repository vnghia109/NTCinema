package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<Object> findByUserIdAndIsActiveIsTrue(String id);

    Optional<User> findByUserName(String userName);

    Optional<User> findByPassword(String password);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    Optional<Object> findByUserNameAndIsActiveIsTrue(String username);
}
