package pe.edu.cibertec.eva.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.cibertec.eva.dto.ChartPoint;
import pe.edu.cibertec.eva.dto.StatusTotals;
import pe.edu.cibertec.eva.repository.AuditLogRepository;
import pe.edu.cibertec.eva.repository.TaskRepository;
import pe.edu.cibertec.eva.service.DashboardService;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {
    private final AuditLogRepository auditLogRepository;
    private final TaskRepository taskRepository;

    @Override
    public StatusTotals getSnapshot(Long userId) {
        long assigned = 0;
        long inProgress = 0;
        long done = 0;

        for (Object[] row : taskRepository.countByStatusForUser(userId)) {
            String status = String.valueOf(row[0]);
            long count = ((Number) row[1]).longValue();
            switch (status) {
                case "ASSIGNED"    -> assigned  = count;
                case "IN_PROGRESS" -> inProgress = count;
                case "DONE"        -> done       = count;
                default -> log.warn("Estado no esperado: {}", status);
            }
        }
        return new StatusTotals(assigned, inProgress, done);
    }

    @Override
    public List<ChartPoint> getWeekly(LocalDate from, LocalDate to, Long userId) {
        LocalDateTime f = from.atStartOfDay();
        LocalDateTime t = to.plusDays(1).atStartOfDay();

        return auditLogRepository.countStatusByDay(f, t, userId).stream()
                .map(r -> new ChartPoint(
                        ((Date) r[0]).toLocalDate(),
                        String.valueOf(r[1]),
                        ((Number) r[2]).longValue()))
                .toList();
    }
}
