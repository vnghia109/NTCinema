package vn.iostar.NT_cinema.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "userStats")
public class UserStats {
    @Id
    private String userStatsId;

    @DBRef
    private User user;
    private BigDecimal totalSpent;
    private int totalOfBookings;
    private int totalOfTickets;

    public UserStats(User user, BigDecimal totalSpent, int totalOfBookings, int totalOfTickets) {
        this.user = user;
        this.totalSpent = totalSpent;
        this.totalOfBookings = totalOfBookings;
        this.totalOfTickets = totalOfTickets;
    }
}
