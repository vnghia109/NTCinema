package vn.iostar.NT_cinema.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.iostar.NT_cinema.constant.ShowStatus;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "showTime")
public class  ShowTime {
    @Id
    private String showTimeId;

    @DBRef
    private Room room;

    @DBRef
    private Movie movie;

    private Date timeStart;

    private Date timeEnd;

    private boolean isSpecial;

    private ShowStatus status;

    private boolean isDelete;

    private Date createdAt;

    private Date updatedAt;
}
