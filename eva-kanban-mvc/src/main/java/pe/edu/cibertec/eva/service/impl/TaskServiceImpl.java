package pe.edu.cibertec.eva.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import pe.edu.cibertec.eva.dto.Role;
import pe.edu.cibertec.eva.dto.Status;
import pe.edu.cibertec.eva.dto.Task;
import pe.edu.cibertec.eva.dto.User;
import pe.edu.cibertec.eva.repository.TaskRepository;
import pe.edu.cibertec.eva.service.TaskService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repo;

    public TaskServiceImpl(TaskRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Task> findAllFor(User user) {
        if (user == null)
            return Collections.emptyList();
        if (user.getRole() == Role.ADMIN) {
            return repo.findAll();
        }
        return repo.findAllFor(user.getUsername());
    }

    @Override
    public Map<String, Long> metricsFor(User user) {
        List<Task> tasks = findAllFor(user);
        return tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getStatus().name(),
                        Collectors.counting()
                ));
    }

    @Override
    public Task create(Task task, User current) {
        if (task.getStatus() == null) task.setStatus(Status.ASSIGNED);
        if (!StringUtils.hasText(task.getUsername()) && current != null) {
            task.setUsername(current.getUsername());
        }
        return repo.save(task);
    }

    @Override
    public Task update(Task task) {
        return repo.update(task);
    }

    @Override
    public Task findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Task no encontrada"));
    }

    @Override
    public Task updateStatus(Long id, Status newStatus) {
        Task t = findById(id);
        t.setStatus(newStatus);
        return repo.update(t);
    }

    @Override
    public Task reassign(Long id, String newUsername) {
        Task t = findById(id);
        t.setUsername(newUsername);
        return repo.update(t);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
