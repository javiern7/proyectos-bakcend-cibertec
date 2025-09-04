package pe.edu.cibertec.eva.repository;

import pe.edu.cibertec.eva.dto.Task;
import java.util.*;

public interface TaskRepository {
    List<Task> findAll();
    List<Task> findAllFor(String username);
    Optional<Task> findById(Long id);
    Task save(Task task);   // create
    Task update(Task task); // update
    void deleteById(Long id);
}
