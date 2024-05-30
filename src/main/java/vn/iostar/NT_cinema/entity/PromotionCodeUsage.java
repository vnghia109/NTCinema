package vn.iostar.NT_cinema.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "promotionCodeUsage")
public class PromotionCodeUsage {
    @Id
    private String id;
    private String userId;
    private String promotionCodeId;
    private LocalDate dateUsed;
}
