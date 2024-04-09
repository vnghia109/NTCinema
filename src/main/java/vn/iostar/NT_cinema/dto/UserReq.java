package vn.iostar.NT_cinema.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import vn.iostar.NT_cinema.entity.Address;

import java.util.Date;
import java.util.List;

@Data
public class UserReq {
    private String fullName;

    private String userName;

    private List<MultipartFile> image;

    private String phone;

    private String email;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date dob;

    private Address address;
}
