package pe.edu.cibertec.eva.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.eva.dto.*;
import pe.edu.cibertec.eva.repository.TaskRepository;
import pe.edu.cibertec.eva.repository.UserRepository;
import pe.edu.cibertec.eva.service.AuditLogService;
import pe.edu.cibertec.eva.service.TaskService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository tasks;
    private final UserRepository users;
    private final AuditLogService audit;

    public TaskServiceImpl(TaskRepository tasks, UserRepository users, AuditLogService audit) {
        this.tasks = tasks; this.users = users; this.audit = audit;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findAllFor(User user) {
        boolean admin = "ADMIN".equalsIgnoreCase(user.getRole());
        return admin ? tasks.findAllWithOwner()
                : tasks.findAllWithOwnerByOwnerUsernameOrderByUpdatedAtDesc(user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> metricsFor(User user) {
        boolean admin = "ADMIN".equalsIgnoreCase(user.getRole());
        Map<String, Long> m = new HashMap<>();
        for (Status s : Status.values()) {
            long c = admin ? tasks.countByStatus(s)
                    : tasks.countByStatusAndOwnerUsername(s, user.getUsername());
            m.put(s.name(), c);
        }
        return m;
    }

    @Override
    @Transactional
    public Task create(Task t, User current, Long ownerId, Long assigneeId) {
        if (t.getStatus() == null) t.setStatus(Status.ASSIGNED);

        // 1) OWNER (si no eres ADMIN, se fuerza a ti)
        User finalOwner = resolveOwnerForCreate(t, current, ownerId);

        // 2) ASSIGNED TO (si no eres ADMIN, también se fuerza a ti)
        User finalAssignee = resolveAssigneeForCreate(t, current, assigneeId);

        t.setOwner(finalOwner);
        t.setAssignedTo(finalAssignee);

        Task saved = tasks.save(t);
        if (audit != null) audit.record(AuditAction.CREATE_TASK, saved, current, null, t.getStatus(), null);
        return saved;
    }

    @Override
    public Task update(Task t, User current, Long ownerId, Long assigneeId) {
        Task db = tasks.findById(t.getId()).orElseThrow(() -> new IllegalArgumentException("Task no existe: " + t.getId()));

        // Campos editables
        if (t.getTitle() != null) db.setTitle(t.getTitle());
        if (t.getDescription() != null) db.setDescription(t.getDescription());
        if (t.getStatus() != null) db.setStatus(t.getStatus());

        // OWNER: solo ADMIN puede cambiarlo
        if (isAdmin(current)) {
            Long candidateOwnerId = ownerId != null ? ownerId : (t.getOwner() != null ? t.getOwner().getId() : null);
            if (candidateOwnerId != null) {
                db.setOwner(users.findById(candidateOwnerId).orElseThrow());
            }
        }

        // ASSIGNED TO: solo ADMIN puede cambiarlo libremente; USER no cambia a terceros
        if (isAdmin(current)) {
            Long candidateAssigneeId = assigneeId != null ? assigneeId : (t.getAssignedTo() != null ? t.getAssignedTo().getId() : null);
            if (candidateAssigneeId != null) {
                db.setAssignedTo(users.findById(candidateAssigneeId).orElseThrow());
            }
        } else {
            // Si no es admin, opcional: forzar que assignedTo sea el mismo current
            // db.setAssignedTo(current);
        }

        Task saved = tasks.save(db);
        if (audit != null) audit.record(AuditAction.UPDATE_TASK, saved, current, null, null, null);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Task findById(Long id) {
        return tasks.findById(id).orElseThrow(() -> new IllegalArgumentException("Task no existe: " + id));
    }

    // ===== Compat sin actor =====
    @Override public Task updateStatus(Long id, Status newStatus){ return updateStatus(id, newStatus, null); }
    @Override public Task reassign(Long id, String newUsername){ return reassign(id, newUsername, null); }
    @Override public void delete(Long id){ delete(id, null); }

    // ===== Con actor (auditoría) =====
    @Override @Transactional
    public Task updateStatus(Long id, Status newStatus, User actor) {
        Task e = tasks.findById(id).orElseThrow();
        Status old = e.getStatus();
        e.setStatus(newStatus);
        Task saved = tasks.save(e);
        audit.record(AuditAction.STATUS_CHANGE, saved, actor, old, newStatus, null);
        return saved;
    }

    @Override @Transactional
    public Task reassign(Long id, String newUsername, User actor) {
        Task e = tasks.findById(id).orElseThrow();
        User newOwner = users.findByUsername(newUsername).orElseThrow();
        User oldOwner = e.getOwner();
        e.setOwner(newOwner);
        Task saved = tasks.save(e);
        audit.record(AuditAction.UPDATE_TASK, saved, actor, null, null,
                "reassign from=" + (oldOwner!=null?oldOwner.getUsername():null) + " to=" + newUsername);
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id, User actor) {
        Task e = tasks.findById(id).orElseThrow();
        tasks.delete(e);
        audit.record(AuditAction.DELETE_TASK, e, actor, e.getStatus(), null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAssigned() {
        return tasks.countByStatus(Status.ASSIGNED);
    }

    @Override
    @Transactional(readOnly = true)
    public long countInProgress() {
        return tasks.countByStatus(Status.IN_PROGRESS);
    }

    @Override
    @Transactional(readOnly = true)
    public long countDone() {
        return tasks.countByStatus(Status.DONE);
    }

    // ==== helpers ====

    private boolean isAdmin(User u) {
        return u != null && "ADMIN".equalsIgnoreCase(u.getRole());
    }

    private User resolveOwnerForCreate(Task t, User current, Long ownerId) {
        if (!isAdmin(current)) return current;

        Long candidate = ownerId != null
                ? ownerId
                : (t.getOwner() != null ? t.getOwner().getId() : null);

        if (candidate != null)
            return users.findById(candidate).orElseThrow();
        return current; // fallback
    }

    private User resolveAssigneeForCreate(Task t, User current, Long assigneeId) {
        if (!isAdmin(current))
            return current;

        Long candidate = assigneeId != null
                ? assigneeId
                : (t.getAssignedTo() != null ? t.getAssignedTo().getId() : null);

        if (candidate != null) return users.findById(candidate).orElseThrow();
        return current; // fallback
    }
}