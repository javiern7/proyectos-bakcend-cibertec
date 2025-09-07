package pe.edu.cibertec.eva.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.cibertec.eva.entity.Status;
import pe.edu.cibertec.eva.entity.TaskEntity;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity,Long> {
    // Trae todas las tareas con su owner ya inicializado
    @Query("select t from TaskEntity t join fetch t.owner")
    List<TaskEntity> findAllWithOwner();

    // Si necesitas por estado (para columnas del Kanban)
    @Query("""
       select t
       from TaskEntity t
       join fetch t.owner o
       left join fetch t.assignedTo a
       where o.id = :userId or (a is not null and a.id = :userId)
    """)
    List<TaskEntity> findAllVisibleFor(@Param("userId") Long userId);

    // Métricas (sin JOIN FETCH porque solo cuentas)
    long countByStatus(Status status);
    @Query("""
       select count(t)
       from TaskEntity t
       where t.status = :status
         and (t.owner.id = :userId or (t.assignedTo is not null and t.assignedTo.id = :userId))
    """)
    long countByStatusAndUser(@Param("status") Status status, @Param("userId") Long userId);


    @Query("select t from TaskEntity t join fetch t.owner " +
            "where t.owner.username = :username order by t.updatedAt desc")
    List<TaskEntity> findAllWithOwnerByOwnerUsernameOrderByUpdatedAtDesc(@Param("username") String username);

    // ajusta el nombre real de tu tabla. Si usas @Table(name="tasks"), usa "tasks"
    @Query(value = """
      SELECT DATE(t.updated_at) AS d,
             SUM(CASE WHEN t.status = 'ASSIGNED'    THEN 1 ELSE 0 END) AS assigned,
             SUM(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS inProgress,
             SUM(CASE WHEN t.status = 'DONE'        THEN 1 ELSE 0 END) AS done
      FROM tasks t
      WHERE (t.assigned_to_id = :userId OR t.owner_id = :userId)
        AND t.updated_at >= DATE_SUB(CURDATE(), INTERVAL :days DAY)
      GROUP BY DATE(t.updated_at)
      ORDER BY d
    """, nativeQuery = true)
    List<Object[]> weeklyStatusCounts(@Param("userId") Long userId, @Param("days") int days);
}
