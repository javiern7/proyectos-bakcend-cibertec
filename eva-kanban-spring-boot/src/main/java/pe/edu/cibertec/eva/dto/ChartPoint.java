package pe.edu.cibertec.eva.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartPoint {
    @JsonProperty("day")
    private LocalDate date;   // día
    private String status;    // ASSIGNED, IN_PROGRESS, DONE
    private long count;

}
