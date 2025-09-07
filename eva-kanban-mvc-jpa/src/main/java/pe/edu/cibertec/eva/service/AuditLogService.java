package pe.edu.cibertec.eva.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.edu.cibertec.eva.dto.AuditDto;
import pe.edu.cibertec.eva.entity.Status;
import pe.edu.cibertec.eva.entity.Task;
import pe.edu.cibertec.eva.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface AuditLogService {
    void logCreate(User actor, Task task);

    void logUpdate(User actor, Task task);

    void logStatusChange(User actor, Task task, Status oldStatus, Status newStatus);

    void logReassign(User actor, Task task, String newAssigneeUsername);

    void logDelete(User actor, Task task);

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


    //void logStatusChange(Long actorId, String actorUsername,
                         //Long taskId, String oldStatus, String newStatus, String details);
    //void logEdit(Long actorId, String actorUsername, Long taskId, String details);
    //void logGeneric(String action, Long actorId, String actorUsername, Long taskId, String details);
}
