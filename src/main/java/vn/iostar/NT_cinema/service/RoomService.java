package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.RoomReq;
import vn.iostar.NT_cinema.entity.Cinema;
import vn.iostar.NT_cinema.entity.Room;
import vn.iostar.NT_cinema.repository.CinemaRepository;
import vn.iostar.NT_cinema.repository.RoomRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    CinemaRepository cinemaRepository;


    public ResponseEntity<GenericResponse> addRoom(RoomReq roomReq) {
        try {
            Optional<Cinema> optionalCinema = cinemaRepository.findById(roomReq.getCinemaId());
            if (optionalCinema.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Cinema not found")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            Optional<Room> roomOptional = roomRepository.findByRoomNameAndCinemaCinemaId(roomReq.getRoomName(), roomReq.getCinemaId());
            if (roomOptional.isPresent()){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Room is available")
                        .result(null)
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }
            Room nRoom = new Room();
            nRoom.setRoomName(roomReq.getRoomName());
            nRoom.setCinema(optionalCinema.get());

            Room room = roomRepository.save(nRoom);

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Add room success")
                    .result(room)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                        .success(false)
                        .message(e.getMessage())
                        .result(null)
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .build());
        }
    }

    public ResponseEntity<GenericResponse> deleteRoom(String roomId) {
        try {
            Optional<Room> roomOptional = roomRepository.findById(roomId);
            if (roomOptional.isPresent()){
                roomRepository.deleteById(roomId);
                return ResponseEntity.ok().body(GenericResponse.builder()
                        .success(true)
                        .message("Delete room success!")
                        .result(null)
                        .statusCode(HttpStatus.OK.value())
                        .build());
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Room not found")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
}
