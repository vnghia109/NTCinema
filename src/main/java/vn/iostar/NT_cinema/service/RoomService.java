package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.RoomReq;
import vn.iostar.NT_cinema.dto.UpdateRoomReq;
import vn.iostar.NT_cinema.entity.*;
import vn.iostar.NT_cinema.repository.*;

import java.util.*;

@Service
public class RoomService {
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    CinemaRepository cinemaRepository;
    @Autowired
    ManagerRepository managerRepository;
    @Autowired
    ShowTimeRepository showTimeRepository;
    @Autowired
    SeatRepository seatRepository;


    public ResponseEntity<GenericResponse> addRoomByManager(RoomReq roomReq, String managerId) {
        try {
            Optional<Manager> manager = managerRepository.findById(managerId);
            Optional<Cinema> optionalCinema = cinemaRepository.findById(manager.get().getCinema().getCinemaId());
            if (optionalCinema.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Rạp phim không tồn tại.")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            Optional<Room> roomOptional = roomRepository.findByRoomNameAndCinema(roomReq.getRoomName(), optionalCinema.get());
            if (roomOptional.isPresent()){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Phòng chiếu đã tồn tại.")
                        .result(null)
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }
            Room nRoom = new Room();
            nRoom.setRoomName(roomReq.getRoomName());
            nRoom.setCinema(optionalCinema.get());
            nRoom.setRowSeat(roomReq.getRowSeat());
            nRoom.setColSeat(roomReq.getColSeat());
            nRoom.setCreateAt(new Date());
            nRoom.setUpdateAt(new Date());

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

    public ResponseEntity<GenericResponse> updateRoom(String roomId, UpdateRoomReq updateRoomReq) {
        try {
            Optional<Room> roomOptional = roomRepository.findById(roomId);
            if (roomOptional.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Phòng không tồn tại.")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }

            List<ShowTime> showTimes = showTimeRepository.findAllByRoomIn(List.of(roomOptional.get()));
            List<Seat> seats = seatRepository.findAllByShowTimeIn(showTimes);
            if (!seats.isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                        .success(false)
                        .message("Phòng đang được sử dụng. Không thể sửa.")
                        .result(null)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build());
            }
            Room room = roomOptional.get();
            room.setRoomName(updateRoomReq.getRoomName());
            room.setRowSeat(updateRoomReq.getRowSeat());
            room.setColSeat(updateRoomReq.getColSeat());
            room.setUpdateAt(new Date());
            roomRepository.save(room);

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Cập nhật thông tin phòng chiếu thành công!")
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

    public ResponseEntity<GenericResponse> updateIsDeleteRoom(String roomId) {
        try {
            Optional<Room> roomOptional = roomRepository.findById(roomId);
            if (roomOptional.isPresent()){
                roomOptional.get().setDelete(!roomOptional.get().isDelete());
                roomOptional.get().setUpdateAt(new Date());
                Room room = roomRepository.save(roomOptional.get());
                return ResponseEntity.ok().body(GenericResponse.builder()
                        .success(true)
                        .message("Update status room success!")
                        .result(room)
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

    public ResponseEntity<GenericResponse> getRooms(Pageable pageable) {
        try {
            Page<Room> rooms = roomRepository.findAllByOrderByRoomIdDesc(pageable);

            Map<String, Object> map = new HashMap<>();
            map.put("content", rooms.getContent());
            map.put("pageNumber", rooms.getPageable().getPageNumber() + 1);
            map.put("pageSize", rooms.getSize());
            map.put("totalPages", rooms.getTotalPages());
            map.put("totalElements", rooms.getTotalElements());

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Get all room success")
                    .result(map)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> getRoomsOfManager(boolean isDelete, String id, Pageable pageable) {
        try {
            Optional<Manager> manager = managerRepository.findById(id);
            Page<Room> rooms = roomRepository.findAllByCinemaAndIsDeleteOrderByRoomIdDesc(manager.get().getCinema(), isDelete, pageable);

            Map<String, Object> map = new HashMap<>();
            map.put("content", rooms.getContent());
            map.put("pageNumber", rooms.getPageable().getPageNumber() + 1);
            map.put("pageSize", rooms.getSize());
            map.put("totalPages", rooms.getTotalPages());
            map.put("totalElements", rooms.getTotalElements());

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Get all room success")
                    .result(map)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> getRoom(String id) {
        try {
            Optional<Room> roomOptional = roomRepository.findById(id);
            return roomOptional.map(room -> ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Get room success")
                    .result(room)
                    .statusCode(HttpStatus.OK.value())
                    .build())).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                    .success(false)
                    .message("Room not found")
                    .result(null)
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .build()));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> findRoomsByCinema(boolean isDelete, String id, Pageable pageable) {
        try {
            Page<Room> rooms = roomRepository.findAllByCinema_CinemaIdAndIsDeleteOrderByRoomIdDesc(id, isDelete, pageable);
            Map<String, Object> map = new HashMap<>();
            map.put("content", rooms.getContent());
            map.put("pageNumber", rooms.getPageable().getPageNumber() + 1);
            map.put("pageSize", rooms.getSize());
            map.put("totalPages", rooms.getTotalPages());
            map.put("totalElements", rooms.getTotalElements());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Lấy danh sách phòng thành công!")
                            .result(map)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }

    public ResponseEntity<GenericResponse> addRoomByAdmin(RoomReq roomReq) {
        try {
            Optional<Cinema> optionalCinema = cinemaRepository.findById(roomReq.getCinemaId());
            if (optionalCinema.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Rạp phim không tồn tại.")
                        .result(null)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            Optional<Room> roomOptional = roomRepository.findByRoomNameAndCinema(roomReq.getRoomName(), optionalCinema.get());
            if (roomOptional.isPresent()){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Phòng "+ roomReq.getRoomName() +" đã tồn tại")
                        .result(null)
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }
            Room nroom = new Room();
            nroom.setRoomName(roomReq.getRoomName());
            nroom.setCinema(optionalCinema.get());
            nroom.setRowSeat(roomReq.getRowSeat());
            nroom.setColSeat(roomReq.getColSeat());
            nroom.setCreateAt(new Date());
            nroom.setUpdateAt(new Date());

            Room room = roomRepository.save(nroom);

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Thêm phòng chiếu thành công!")
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
}
