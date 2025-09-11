package pe.edu.cibertec.eva.service;

import pe.edu.cibertec.eva.dto.ChartPoint;
import pe.edu.cibertec.eva.dto.StatusTotals;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    StatusTotals getSnapshot(Long userId);                             // estado actual (como el tablero)
    List<ChartPoint> getWeekly(LocalDate from, LocalDate to, Long userId); // evolución (audit_log)
}
