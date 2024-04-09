package vn.iostar.NT_cinema.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Role;
import vn.iostar.NT_cinema.entity.User;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<Object> findByUserIdAndIsActiveIsTrue(String id);

    Optional<User> findByUserName(String userName);

    Optional<User> findByPassword(String password);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    Optional<Object> findByUserNameAndIsActiveIsTrue(String username);

    List<User> findByIsActiveFalseAndCreatedAtBefore(Date twentyFourHoursAgo);

    Page<User> findAllByOrderByLastLoginAtDesc(@NotNull Pageable pageable);

    Page<User> findAllByRoleIn(List<Role> roles, @NotNull Pageable pageable);

    boolean existsUserByEmail(String email);

    boolean existsUserByPhone(String phone);

    boolean existsUserByUserName(String userName);
}
