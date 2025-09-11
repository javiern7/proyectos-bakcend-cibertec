package pe.edu.cibertec.eva.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusTotals {
    private long assigned;
    private long inProgress;
    private long done;
}
