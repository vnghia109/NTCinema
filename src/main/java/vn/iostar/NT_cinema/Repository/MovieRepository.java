package vn.iostar.NT_cinema.Repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.NT_cinema.Entity.Movie;

@Repository
public interface MovieRepository extends MongoRepository<Movie, ObjectId> {
}
