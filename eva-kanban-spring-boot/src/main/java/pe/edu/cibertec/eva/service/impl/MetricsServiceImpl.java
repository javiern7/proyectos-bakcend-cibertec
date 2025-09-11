package pe.edu.cibertec.eva.service.impl;

import org.springframework.stereotype.Service;
import pe.edu.cibertec.eva.repository.TaskRepository;
import pe.edu.cibertec.eva.service.MetricsService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
public class MetricsServiceImpl implements MetricsService {
    private final TaskRepository taskRepo;
    public MetricsServiceImpl(TaskRepository taskRepo) { this.taskRepo = taskRepo; }

    @Override
    public Map<String, Object> weeklyForUser(Long userId, int days) {
        // normaliza a 7 días atrás -> hoy
        int window = Math.max(1, days);
        LocalDate today = LocalDate.now();
        List<LocalDate> labels = IntStream.rangeClosed(window-1, 0)
                .mapToObj(today::minusDays).toList();

        Map<LocalDate, int[]> buckets = new LinkedHashMap<>();
        labels.forEach(d -> buckets.put(d, new int[]{0,0,0}));

        for (Object[] row : taskRepo.weeklyStatusCounts(userId, window)) {
            LocalDate d = ((Date) row[0]).toLocalDate();
            int a = ((Number) row[1]).intValue();
            int p = ((Number) row[2]).intValue();
            int f = ((Number) row[3]).intValue();
            buckets.replace(d, new int[]{a, p, f});
        }

        Map<String,Object> out = new HashMap<>();
        out.put("labels", labels.stream().map(ld -> ld.getDayOfWeek().name().substring(0,3)).toList());
        out.put("assigned",  labels.stream().map(ld -> buckets.get(ld)[0]).toList());
        out.put("inProgress",labels.stream().map(ld -> buckets.get(ld)[1]).toList());
        out.put("done",      labels.stream().map(ld -> buckets.get(ld)[2]).toList());
        return out;
    }
}
