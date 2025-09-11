package pe.edu.cibertec.eva.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.cibertec.eva.entity.AuditLogEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity,Long>, JpaSpecificationExecutor<AuditLogEntity> {

    // últimos N días (ordenado del más reciente)
    @Query("SELECT a FROM AuditLogEntity a WHERE a.createdAt >= :from ORDER BY a.createdAt DESC")
    List<AuditLogEntity> findSince(@Param("from") LocalDateTime from);

    // si quieres paginar:
    @Query(value = """
    SELECT DATE(al.created_at) AS d, al.new_status AS s, COUNT(*) AS c
      FROM audit_log al
      JOIN tasks t ON t.id = al.task_id
     WHERE al.created_at >= :from
       AND al.created_at <  :to
       AND (:userId IS NULL OR t.assigned_to_id = :userId)
       AND al.new_status IS NOT NULL
     GROUP BY d, s
     ORDER BY d
    """, nativeQuery = true)
    List<Object[]> countStatusByDay(@Param("from") LocalDateTime from,
                                    @Param("to")   LocalDateTime to,
                                    @Param("userId") Long userId);
}
