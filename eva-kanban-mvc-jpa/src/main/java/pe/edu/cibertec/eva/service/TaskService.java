package pe.edu.cibertec.eva.service;

import pe.edu.cibertec.eva.entity.Status;
import pe.edu.cibertec.eva.entity.Task;
import pe.edu.cibertec.eva.entity.User;
import java.util.*;


public interface TaskService {
    List<Task> findAllFor(User user);       // trae tareas con owner ya cargado
    Map<String, Long> metricsFor(User user);
    //Task create(Task task, User current, Long ownerId);
    //Task update(Task task, User current, Long ownerId);
    Task create(Task t, User current, Long ownerId, Long assigneeId);
    Task update(Task t, User current, Long ownerId, Long assigneeId);
    Task findById(Long id);
    Task updateStatus(Long id, Status newStatus);            // compat
    Task updateStatus(Long id, Status newStatus, User actor); // con actor (auditoría)
    Task reassign(Long id, String newUsername);              // compat
    Task reassign(Long id, String newUsername, User actor);   // con actor (auditoría)
    void delete(Long id);
    void delete(Long id, User actor);
    //long countAssigned();
    //long countInProgress();
    //long countDone();
}
