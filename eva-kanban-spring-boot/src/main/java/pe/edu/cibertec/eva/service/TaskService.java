package pe.edu.cibertec.eva.service;

import pe.edu.cibertec.eva.entity.Status;
import pe.edu.cibertec.eva.entity.TaskEntity;
import pe.edu.cibertec.eva.entity.UserEntity;

import java.util.List;
import java.util.Map;


public interface TaskService {
    List<TaskEntity> findAllFor(UserEntity user);       // trae tareas con owner ya cargado
    Map<String, Long> metricsFor(UserEntity user);
    //Task create(Task task, User current, Long ownerId);
    //Task update(Task task, User current, Long ownerId);
    TaskEntity create(TaskEntity t, UserEntity current, Long ownerId, Long assigneeId);
    TaskEntity update(TaskEntity t, UserEntity current, Long ownerId, Long assigneeId);
    TaskEntity findById(Long id);
    TaskEntity updateStatus(Long id, Status newStatus);            // compat
    TaskEntity updateStatus(Long id, Status newStatus, UserEntity actor); // con actor (auditoría)
    TaskEntity reassign(Long id, String newUsername);              // compat
    TaskEntity reassign(Long id, String newUsername, UserEntity actor);   // con actor (auditoría)
    void delete(Long id);
    void delete(Long id, UserEntity actor);
    //long countAssigned();
    //long countInProgress();
    //long countDone();
}
