package pe.edu.cibertec.eva.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.eva.dto.AuditDto;
import pe.edu.cibertec.eva.entity.*;
import pe.edu.cibertec.eva.repository.AuditLogRepository;
import pe.edu.cibertec.eva.service.AuditLogService;
import pe.edu.cibertec.eva.service.AuditSpecs;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository repo;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    @PersistenceContext
    private EntityManager em;


    public AuditLogServiceImpl(AuditLogRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public void logCreate(User actor, Task task) {
        save("CREATE", actor, task, null, null, safe("Creación de tarea"));
    }

    @Override
    @Transactional
    public void logUpdate(User actor, Task task) {
        save("UPDATE", actor, task, null, null, safe("Actualización de tarea"));
    }

    @Override
    @Transactional
    public void logStatusChange(User actor, Task task, Status oldStatus, Status newStatus) {
        save("STATUS_CHANGE", actor, task, safe(oldStatus), safe(newStatus), safe("Cambio de estado"));
    }

    @Override
    @Transactional
    public void logReassign(User actor, Task task, String newAssigneeUsername) {
        String details = "Reasignada a: " + safe(newAssigneeUsername);
        save("REASSIGN", actor, task, null, null, details);
    }

    @Override
    @Transactional
    public void logDelete(User actor, Task task) {
        save("DELETE", actor, task, safe(task != null ? task.getStatus() : null), null, safe("Eliminación de tarea"));
    }

    @Override
    @Transactional
    public void logCustom(String action, Long actorId, String actorUsername, Long taskId,
                          String details, String oldStatus, String newStatus) {
        AuditLog a = new AuditLog();
        a.setAction(nvl(action, "CUSTOM"));
        a.setActorId(actorId);
        a.setActorUsername(actorUsername);
        a.setTaskId(taskId);
        a.setDetails(details);
        a.setOldStatus(oldStatus);
        a.setNewStatus(newStatus);
        a.setCreatedAt(LocalDateTime.now());
        repo.save(a);
    }

    @Override
    public Page<AuditDto> search(Integer days, Long actorId, Long taskId,
                                 String action, String oldStatus, String newStatus,
                                 Pageable pageable) {
        Specification<AuditLog> spec = AuditSpecs.filter(days, actorId, taskId, action, oldStatus, newStatus);
        return repo.findAll(spec, pageable).map(this::toDto);
    }

    private AuditDto toDto(AuditLog a) {
        return new AuditDto(
                a.getCreatedAt() != null ? a.getCreatedAt().format(fmt) : "",
                a.getActorUsername(),
                a.getAction(),
                a.getTaskId(),
                a.getOldStatus(),
                a.getNewStatus(),
                a.getDetails()
        );
    }

    private void save(String action, User actor, Task task, String oldStatus, String newStatus, String details) {
        AuditLog a = new AuditLog();
        a.setAction(nvl(action, "CUSTOM"));
        if (actor != null) {
            a.setActorId(actor.getId());
            a.setActorUsername(actor.getUsername());
        }
        if (task != null) {
            a.setTaskId(task.getId());
        }
        a.setOldStatus(oldStatus);
        a.setNewStatus(newStatus);
        a.setDetails(details);
        a.setCreatedAt(LocalDateTime.now());
        repo.save(a);
    }

    private static String safe(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String nvl(String s, String d) {
        return (s == null || s.isEmpty()) ? d : s;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditDto> search(Long actorId,
                                 Long taskId,
                                 String action,
                                 String oldStatus,
                                 String newStatus,
                                 Integer days) {

        int d = (days == null || days <= 0) ? 7 : days;
        LocalDateTime from = LocalDate.now().minusDays(d).atStartOfDay();
        LocalDateTime to   = LocalDateTime.now(); // ahora

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AuditLog> cq = cb.createQuery(AuditLog.class);
        Root<AuditLog> root = cq.from(AuditLog.class);

        List<Predicate> where = new ArrayList<>();

        // Rango de fechas (cuando >= from AND cuando <= to)
        where.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        where.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));

        if (actorId != null) {
            where.add(cb.equal(root.get("actorId"), actorId));
        }
        if (taskId != null) {
            where.add(cb.equal(root.get("taskId"), taskId));
        }
        if (action != null && !action.isBlank()) {
            where.add(cb.equal(root.get("action"), action));
        }
        if (oldStatus != null && !oldStatus.isBlank()) {
            where.add(cb.equal(root.get("oldStatus"), oldStatus));
        }
        if (newStatus != null && !newStatus.isBlank()) {
            where.add(cb.equal(root.get("newStatus"), newStatus));
        }

        cq.where(where.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("createdAt")));

        List<AuditLog> list = em.createQuery(cq).getResultList();

        // Mapear a tu record AuditDto (when como String "yyyy-MM-dd HH:mm")
        List<AuditDto> out = new ArrayList<>(list.size());
        for (AuditLog a : list) {
            String whenStr = a.getCreatedAt() != null ? a.getCreatedAt().format(fmt) : null;
            out.add(new AuditDto(
                    whenStr,
                    a.getActorUsername(),
                    a.getAction(),
                    a.getTaskId(),
                    a.getOldStatus(),
                    a.getNewStatus(),
                    a.getDetails()
            ));
        }
        return out;
    }
}
