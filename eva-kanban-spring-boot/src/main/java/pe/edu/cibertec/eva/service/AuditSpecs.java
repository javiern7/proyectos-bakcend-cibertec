package pe.edu.cibertec.eva.service;

import org.springframework.data.jpa.domain.Specification;
import pe.edu.cibertec.eva.entity.AuditLogEntity;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public final class AuditSpecs {
    private AuditSpecs() {}

    public static Specification<AuditLogEntity> filter(Integer days, Long actorId, Long taskId,
                                                       String action, String oldStatus, String newStatus) {
        return (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (days != null && days > 0) {
                ps.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                        LocalDate.now().minusDays(days).atStartOfDay()));
            }
            if (actorId != null)   ps.add(cb.equal(root.get("actorId"), actorId));
            if (taskId != null)    ps.add(cb.equal(root.get("taskId"), taskId));
            if (action != null && !action.isBlank())
                ps.add(cb.equal(root.get("action"), action));
            if (oldStatus != null && !oldStatus.isBlank())
                ps.add(cb.equal(root.get("oldStatus"), oldStatus));
            if (newStatus != null && !newStatus.isBlank())
                ps.add(cb.equal(root.get("newStatus"), newStatus));
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
