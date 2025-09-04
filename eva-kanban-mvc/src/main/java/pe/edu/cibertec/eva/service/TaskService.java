package pe.edu.cibertec.eva.service;

import pe.edu.cibertec.eva.dto.Status;
import pe.edu.cibertec.eva.dto.Task;
import pe.edu.cibertec.eva.dto.User;
import java.util.*;


public interface  TaskService {
    List<Task> findAllFor(User user);
    Map<String, Long> metricsFor(User user);

    Task create(Task task, User current);
    Task update(Task task);
    Task findById(Long id);

    Task updateStatus(Long id, Status newStatus);
    Task reassign(Long id, String newUsername);
    void delete(Long id);
}
