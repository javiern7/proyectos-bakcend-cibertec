package pe.edu.cibertec.eva.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.cibertec.eva.dto.Status;
import pe.edu.cibertec.eva.dto.Task;
import java.util.*;

public interface TaskRepository extends JpaRepository<Task,Long> {
    List<Task> findByStatusOrderByUpdatedAtDesc(Status status);
    List<Task> findByOwnerUsernameOrderByUpdatedAtDesc(String username);
    @Query("SELECT t FROM Task t JOIN FETCH t.owner")
    // Trae todas las tareas con su owner ya inicializado
    List<Task> findAllWithOwner();
    // Si necesitas por estado (para columnas del Kanban)
    @Query("select t from Task t join fetch t.owner where t.status = :status")
    List<Task> findAllWithOwnerByStatus(@Param("status") Status status);

    // Métricas (sin JOIN FETCH porque solo cuentas)
    long countByStatus(Status status);
    long countByStatusAndOwnerUsername(Status status, String username); // user

    @Query("select t from Task t join fetch t.owner " +
            "where t.owner.username = :username order by t.updatedAt desc")
    List<Task> findAllWithOwnerByOwnerUsernameOrderByUpdatedAtDesc(@Param("username") String username);
}
