package pe.edu.cibertec.eva.controller;

import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.eva.entity.UserEntity;
import pe.edu.cibertec.eva.repository.TaskRepository;
import pe.edu.cibertec.eva.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    private final TaskRepository tasks;
    private final UserService users;

    public MetricsController(TaskRepository tasks, UserService users) {
        this.tasks = tasks;
        this.users = users;
    }

    @GetMapping("/weekly")
    public Map<String, Object> weekly(@SessionAttribute("user") UserEntity user,
                                      @RequestParam(defaultValue = "7") int days) {
        List<Object[]> rows = tasks.weeklyStatusCounts(user.getId(), days);
        List<String> labels = new ArrayList<>();
        List<Integer> assigned = new ArrayList<>(), inProgress = new ArrayList<>(), done = new ArrayList<>();
        for (Object[] r : rows) {
            labels.add(String.valueOf(r[0]));
            assigned.add(((Number) r[1]).intValue());
            inProgress.add(((Number) r[2]).intValue());
            done.add(((Number) r[3]).intValue());
        }
        return Map.of("labels", labels, "assigned", assigned, "inProgress", inProgress, "done", done);
    }
}
