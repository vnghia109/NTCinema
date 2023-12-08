package vn.iostar.NT_cinema.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.entity.Address;

import java.util.Optional;

@Repository
public interface AddressRepository extends MongoRepository<Address, String> {
    Optional<Address> findByStreetAndProvinceAndDistrictAndCountry(String street, String province, String district, String country);
}
