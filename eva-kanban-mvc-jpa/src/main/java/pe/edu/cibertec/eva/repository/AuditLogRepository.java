package pe.edu.cibertec.eva.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.cibertec.eva.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long>, JpaSpecificationExecutor<AuditLog> {

    // últimos N días (ordenado del más reciente)
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt >= :from ORDER BY a.createdAt DESC")
    List<AuditLog> findSince(@Param("from") LocalDateTime from);

    // si quieres paginar:
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt >= :from ORDER BY a.createdAt DESC")
    Page<AuditLog> findSince(@Param("from") LocalDateTime from, Pageable pageable);
}
