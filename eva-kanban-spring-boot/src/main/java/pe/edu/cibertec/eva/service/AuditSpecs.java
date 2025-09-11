package pe.edu.cibertec.eva.service;

import org.springframework.data.jpa.domain.Specification;
import pe.edu.cibertec.eva.entity.AuditLogEntity;
import jakarta.persistence.criteria.Predicate;
import pe.edu.cibertec.eva.util.Functions;
import java.util.ArrayList;
import java.util.List;

public final class AuditSpecs {
    private AuditSpecs() {}

    public static Specification<AuditLogEntity> filter(Integer days, Long actorId, Long taskId,
                                                       String action, String oldStatus, String newStatus) {
        return (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            Functions.addIfNotNull(ps, Functions.fromLastDays(root, cb, "createdAt", days));
            Functions.addIfNotNull(ps, Functions.eq(root, cb, "actorId",   actorId));
            Functions.addIfNotNull(ps, Functions.eq(root, cb, "taskId",    taskId));
            Functions.addIfNotNull(ps, Functions.eq(root, cb, "action",    action));
            Functions.addIfNotNull(ps, Functions.eq(root, cb, "oldStatus", oldStatus));
            Functions.addIfNotNull(ps, Functions.eq(root, cb, "newStatus", newStatus));

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
