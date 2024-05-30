package vn.iostar.NT_cinema.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookReq {
    String code;
    List<String> seatIds;
    List<String> foodIds;
}
