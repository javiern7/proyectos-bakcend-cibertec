package pe.edu.cibertec.eva.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log", indexes = {
        @Index(name="idx_audit_task", columnList="task_id"),
        @Index(name="idx_audit_created", columnList="created_at"),
        @Index(name="idx_audit_actor", columnList="actor_id") // sugerido
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogEntity {
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

}
