package pe.edu.cibertec.eva.entity;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log", indexes = {
        @Index(name="idx_audit_task", columnList="task_id"),
        @Index(name="idx_audit_created", columnList="created_at"),
        @Index(name="idx_audit_actor", columnList="actor_id") // sugerido
})
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action", length = 30, nullable = false)
    private String action;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_username", length = 60)
    private String actorUsername;

    @Column(name = "old_status", length = 20)
    private String oldStatus;

    @Column(name = "new_status", length = 20)
    private String newStatus;

    @Lob
    @Column(name = "details")
    private String details;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public AuditLog setId(Long id) {
        this.id = id;
        return this;
    }

    public String getAction() {
        return action;
    }

    public AuditLog setAction(String action) {
        this.action = action;
        return this;
    }

    public Long getTaskId() {
        return taskId;
    }

    public AuditLog setTaskId(Long taskId) {
        this.taskId = taskId;
        return this;
    }

    public Long getActorId() {
        return actorId;
    }

    public AuditLog setActorId(Long actorId) {
        this.actorId = actorId;
        return this;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public AuditLog setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
        return this;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public AuditLog setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
        return this;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public AuditLog setNewStatus(String newStatus) {
        this.newStatus = newStatus;
        return this;
    }

    public String getDetails() {
        return details;
    }

    public AuditLog setDetails(String details) {
        this.details = details;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public AuditLog setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
