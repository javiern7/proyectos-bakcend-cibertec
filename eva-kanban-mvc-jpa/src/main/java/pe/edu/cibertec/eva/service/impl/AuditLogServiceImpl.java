package pe.edu.cibertec.eva.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.eva.dto.*;
import pe.edu.cibertec.eva.repository.AuditLogRepository;
import pe.edu.cibertec.eva.service.AuditLogService;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository repo;
    public AuditLogServiceImpl(AuditLogRepository repo){ this.repo = repo; }

    @Transactional
    @Override
    public void record(AuditAction action, Task task, User actor,
                       Status oldSt, Status newSt, String details) {
        AuditLog a = new AuditLog();
        a.setAction(action);
        a.setTaskId(task.getId());
        if (actor != null) {
            a.setActorId(actor.getId());
            a.setActorUsername(actor.getUsername());
        }
        a.setOldStatus(oldSt != null ? oldSt.name() : null);
        a.setNewStatus(newSt != null ? newSt.name() : null);
        a.setDetails(details);
        repo.save(a);
    }
}
