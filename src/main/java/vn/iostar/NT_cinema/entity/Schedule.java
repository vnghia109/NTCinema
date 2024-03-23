package vn.iostar.NT_cinema.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "schedule")
public class Schedule {

    private String scheduleId;
    private String showTimeId;
    private Date date;
    private String startTime;
    private String endTime;
    private String roomId;
    private Date createdAt;
    private Date updatedAt;
}
