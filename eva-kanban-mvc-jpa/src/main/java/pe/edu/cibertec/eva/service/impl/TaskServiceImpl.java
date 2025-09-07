package pe.edu.cibertec.eva.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.eva.entity.*;
import pe.edu.cibertec.eva.repository.TaskRepository;
import pe.edu.cibertec.eva.repository.UserRepository;
import pe.edu.cibertec.eva.service.AuditLogService;
import pe.edu.cibertec.eva.service.TaskService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository tasks;
    private final UserRepository users;
    private final AuditLogService audit;

    public TaskServiceImpl(TaskRepository tasks, UserRepository users, AuditLogService audit) {
        this.tasks = requireNonNull(tasks);
        this.users = requireNonNull(users);
        this.audit = requireNonNull(audit);
    }

    private boolean isAdmin(User u) {
        return u != null && u.getRole().equalsIgnoreCase("ADMIN"); // ajusta a tu enum/propiedad real
    }

    private void ensureExists(Task t, Long id) {
        if (t == null) throw new IllegalArgumentException("Task " + id + " no existe");
    }

    private Status nextOf(Status s) {
        if (s == Status.ASSIGNED)
            return Status.IN_PROGRESS;
        if (s == Status.IN_PROGRESS)
            return Status.DONE;
        return null;
    }

    private void ensureForwardOnly(User actor, Task t, Status newStatus) {
        if (isAdmin(actor))
            return; // admin puede todo
        Status next = nextOf(t.getStatus());
        if (next == null || next != newStatus) {
            throw new IllegalStateException("Transición no permitida para usuario");
        }
        // si no es dueño/asignado, tampoco
        if (t.getAssignedTo() == null || !t.getAssignedTo().getId().equals(actor.getId())) {
            throw new IllegalStateException("Solo el dueño puede avanzar su tarea");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Task findById(Long id) {
        return tasks.findById(id).orElseThrow(() -> new IllegalArgumentException("Task no encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findAllFor(User user) {
        // Si admin: todas con owner fetch; si no: propias (owner o assignedTo)
        if (isAdmin(user))
            return tasks.findAllWithOwner();
        return tasks.findAllVisibleFor(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> metricsFor(User user) {
        Map<String, Long> m = new HashMap<>();
        for (Status s : Status.values()) {
            long c = isAdmin(user)
                    ? tasks.countByStatus(s)
                    : tasks.countByStatusAndUser(s, user.getId());
            m.put(s.name(), c);
        }
        return m;
    }

    @Override
    @Transactional
    public Task create(Task t, User actor, Long ownerId, Long assigneeId) {
        if (t.getStatus() == null) t.setStatus(Status.ASSIGNED);

        // OWNER
        User owner = isAdmin(actor)
                ? (ownerId != null ? users.findById(ownerId).orElse(actor) : (t.getOwner() != null ? users.findById(t.getOwner().getId()).orElse(actor) : actor))
                : actor;

        // ASSIGNEE
        User assignee = isAdmin(actor)
                ? (assigneeId != null ? users.findById(assigneeId).orElse(actor) : (t.getAssignedTo() != null ? users.findById(t.getAssignedTo().getId()).orElse(actor) : actor))
                : actor;

        t.setOwner(owner);
        t.setAssignedTo(assignee);

        Task saved = tasks.save(t);
        audit.logCreate(actor, saved); // implementa según tu AuditLogService
        return saved;
    }

    @Override
    @Transactional
    public Task update(Task t, User actor, Long ownerId, Long assigneeId) {
        Task db = findById(t.getId());
        // Solo admin puede reasignar dueños/assignee libremente
        if (isAdmin(actor)) {
            if (ownerId != null) db.setOwner(users.findById(ownerId).orElse(db.getOwner()));
            if (assigneeId != null) db.setAssignedTo(users.findById(assigneeId).orElse(db.getAssignedTo()));
        } else {
            // usuario: bloquear cambios de owner/assignee
            t.setOwner(db.getOwner());
            t.setAssignedTo(db.getAssignedTo());
        }

        db.setTitle(t.getTitle());
        db.setDescription(t.getDescription());

        Task saved = tasks.save(db);
        audit.logUpdate(actor, saved);
        return saved;
    }

    @Override
    @Transactional
    public Task updateStatus(Long id, Status newStatus, User actor) {
        Task db = findById(id);
        ensureForwardOnly(actor, db, newStatus);
        Status old = db.getStatus();
        db.setStatus(newStatus);
        Task saved = tasks.save(db);
        audit.logStatusChange(actor, saved, old, newStatus);
        return saved;
    }

    @Override
    @Transactional
    public Task updateStatus(Long id, Status newStatus) {
        // compat si no te pasan actor
        return updateStatus(id, newStatus, null);
    }

    @Override
    @Transactional
    public Task reassign(Long id, String newUsername, User actor) {
        Task db = findById(id);
        if (!isAdmin(actor)) throw new IllegalStateException("Solo ADMIN puede reasignar");
        User assignee = users.findByUsername(newUsername).orElseThrow();
        db.setAssignedTo(assignee);
        Task saved = tasks.save(db);
        audit.logReassign(actor, saved, newUsername);
        return saved;
    }

    @Override
    @Transactional
    public Task reassign(Long id, String newUsername) {
        return reassign(id, newUsername, null);
    }

    @Override
    @Transactional
    public void delete(Long id, User actor) {
        Task db = findById(id);
        tasks.delete(db);
        audit.logDelete(actor, db);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        delete(id, null);
    }
}