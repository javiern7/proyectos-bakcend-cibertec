package pe.edu.cibertec.eva.service;

import pe.edu.cibertec.eva.dto.AuditAction;
import pe.edu.cibertec.eva.dto.Status;
import pe.edu.cibertec.eva.dto.Task;
import pe.edu.cibertec.eva.dto.User;

public interface AuditLogService {
    void record(AuditAction action,
                Task task,
                User actor,
                Status oldSt,
                Status newSt,
                String details);
}
