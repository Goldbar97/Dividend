package zerobase.dividend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zerobase.dividend.model.Dividend;

import java.time.LocalDateTime;

@Entity(name = "DIVIDEND")
@Getter
@NoArgsConstructor
@ToString
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"companyId", "date"}
                )
        }
)
public class DividendEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long companyId;
    
    private LocalDateTime date;
    
    private String dividend;
    
    public DividendEntity(Long companyId, Dividend dividend) {
        this.companyId = companyId;
        date = dividend.getDate();
        this.dividend = dividend.getDividend();
    }
}
