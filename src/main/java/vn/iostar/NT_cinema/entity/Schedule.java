package vn.iostar.NT_cinema.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "schedule")
public class Schedule {
    @Id
    private String scheduleId;
    private String showTimeId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String roomId;
    private Date createdAt;
    private Date updatedAt;

    public Schedule(String showTimeId, LocalDate date, LocalTime startTime, LocalTime endTime, String roomId) {
        this.showTimeId = showTimeId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomId = roomId;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
}
