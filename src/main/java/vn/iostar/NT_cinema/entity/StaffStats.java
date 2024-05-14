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
@Document(collection = "staffStats")
public class StaffStats {
    @Id
    private String staffStatsId;
    @DBRef
    private Staff staff;
    private int totalOfTickets;
    private BigDecimal revenue;

    public StaffStats(Staff staff, BigDecimal revenue, int totalOfTickets) {
        this.staff = staff;
        this.revenue = revenue;
        this.totalOfTickets = totalOfTickets;
    }
}
