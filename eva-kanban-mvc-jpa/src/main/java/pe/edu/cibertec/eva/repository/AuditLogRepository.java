package pe.edu.cibertec.eva.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.cibertec.eva.dto.AuditLog;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {

    List<AuditLog> findByTaskIdOrderByCreatedAtDesc(Long taskId);
}
