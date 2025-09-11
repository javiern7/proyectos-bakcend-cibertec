package pe.edu.cibertec.eva.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.edu.cibertec.eva.dto.AuditDto;
import pe.edu.cibertec.eva.entity.Status;
import pe.edu.cibertec.eva.entity.TaskEntity;
import pe.edu.cibertec.eva.entity.UserEntity;

import java.util.List;

public interface AuditLogService {
    void logCreate(UserEntity actor, TaskEntity task);

    void logUpdate(UserEntity actor, TaskEntity task);

    void logStatusChange(UserEntity actor, TaskEntity task, Status oldStatus, Status newStatus);

    void logReassign(UserEntity actor, TaskEntity task, String newAssigneeUsername);

    void logDelete(UserEntity actor, TaskEntity task);

    Page<AuditDto> search(Integer days, Long actorId, Long taskId,
                          String action, String oldStatus, String newStatus,
                          Pageable pageable);

    List<AuditDto> search(Long actorId,
                          Long taskId,
                          String action,
                          String oldStatus,
                          String newStatus,
                          Integer days);
    /** Falla‑seguro genérico para casos especiales o utilitarios */
    void logCustom(String action, Long actorId, String actorUsername, Long taskId,
                   String details, String oldStatus, String newStatus);
}
