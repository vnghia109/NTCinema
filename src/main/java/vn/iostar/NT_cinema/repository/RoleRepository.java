package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Role;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {
    Role findByRoleName(String Name);
}
