package vn.iostar.NT_cinema.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookReq {
    List<String> seatIds;
    List<String> foodIds;
}
