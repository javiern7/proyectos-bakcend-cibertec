package pe.edu.cibertec.eva.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pe.edu.cibertec.eva.entity.Status;
import pe.edu.cibertec.eva.entity.TaskEntity;
import pe.edu.cibertec.eva.entity.UserEntity;
import pe.edu.cibertec.eva.repository.TaskRepository;
import pe.edu.cibertec.eva.service.AuditLogService;
import pe.edu.cibertec.eva.util.Constants;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(Constants.LOGGING)
public class LoggingAspect {

    private final AuditLogService audit;
    private final TaskRepository taskRepository;

    // Guarda el estado previo por id de tarea mientras dura la invocación
    private final Map<Long, Status> oldStatusHolder = new ConcurrentHashMap<>();

    /* ===================== CREATE ===================== */
    // Firma del service esperada: TaskEntity create(TaskEntity task, UserEntity actor)
    @AfterReturning(
            pointcut = "execution(* pe.edu.cibertec.eva.service.TaskService.create(..)) && args(task, actor)",
            returning = "saved")
    public void afterCreate(TaskEntity task, UserEntity actor, TaskEntity saved) {
        audit.logCreate(actor, saved);
    }

    /* ===================== UPDATE (datos) ===================== */
    // Firma del service esperada: TaskEntity update(TaskEntity task, UserEntity actor)
    @AfterReturning(
            pointcut = "execution(* pe.edu.cibertec.eva.service.TaskService.update(..)) && args(task, actor)",
            returning = "updated")
    public void afterUpdate(TaskEntity task, UserEntity actor, TaskEntity updated) {
        audit.logUpdate(actor, updated);
    }

    /* ===================== STATUS CHANGE ===================== */
    // Firma del service esperada: void updateStatus(Long id, Status newStatus, UserEntity actor)
    // 1) Capturamos el estado anterior ANTES de ejecutar el método
    @Before("execution(* pe.edu.cibertec.eva.service.TaskService.updateStatus(..)) && args(id, newStatus, actor)")
    public void captureOldStatus(Long id, Status newStatus, UserEntity actor) {
        Optional<TaskEntity> opt = taskRepository.findById(id);
        opt.map(TaskEntity::getStatus).ifPresent(prev -> oldStatusHolder.put(id, prev));
    }

    // 2) Registramos el cambio DESPUÉS de ejecutar el método
    @After("execution(* pe.edu.cibertec.eva.service.TaskService.updateStatus(..)) && args(id, newStatus, actor)")
    public void afterStatusChange(Long id, Status newStatus, UserEntity actor) {
        Status oldStatus = oldStatusHolder.remove(id); // limpiamos siempre
        TaskEntity task = taskRepository.findById(id).orElse(null);
        if (task != null) {
            audit.logStatusChange(actor, task, oldStatus, newStatus);
        } else {
            log.warn("No se encontró la tarea con id={} para auditar cambio de estado", id);
        }
    }

    /* ===================== REASSIGN ===================== */
    // Firma del service esperada: void reassign(Long id, String newAssigneeUsername, UserEntity actor)
    @After("execution(* pe.edu.cibertec.eva.service.TaskService.reassign(..)) && args(id, newAssigneeUsername, actor)")
    public void afterReassign(Long id, String newAssigneeUsername, UserEntity actor) {
        taskRepository.findById(id).ifPresent(task ->
                audit.logReassign(actor, task, newAssigneeUsername)
        );
    }

    /* ===================== DELETE ===================== */
    // Firma del service esperada: void delete(Long id, UserEntity actor)
    // Guardamos referencia antes (porque después puede no existir)
    private final Map<Long, TaskEntity> deleteHolder = new ConcurrentHashMap<>();

    @Before("execution(* pe.edu.cibertec.eva.service.TaskService.delete(..)) && args(id, actor)")
    public void captureForDelete(Long id, UserEntity actor) {
        taskRepository.findById(id).ifPresent(task -> deleteHolder.put(id, task));
    }

    @After("execution(* pe.edu.cibertec.eva.service.TaskService.delete(..)) && args(id, actor)")
    public void afterDelete(Long id, UserEntity actor) {
        TaskEntity task = deleteHolder.remove(id);
        if (task != null) {
            audit.logDelete(actor, task);
        } else {
            log.warn("No se pudo registrar delete: no se encontró snapshot de la tarea id={}", id);
        }
    }
}
