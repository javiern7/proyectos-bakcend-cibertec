package pe.edu.cibertec.eva.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.edu.cibertec.eva.dto.Status;
import pe.edu.cibertec.eva.dto.Task;
import pe.edu.cibertec.eva.dto.User;

@Aspect
@Component
public class LoggingAspect {
  private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

  @AfterReturning(pointcut = "execution(* pe.edu.cibertec.eva.service.TaskService.create(..)) && args(task, user)",
                  returning = "result")
  public void logCreate(Task task, User user, Object result){
    Task saved = (Task) result;
    log.info("TASK-CREATE by={} id={} title='{}' assignee={}", user.getUsername(), saved.getId(), saved.getTitle(), saved.getUsername());
  }

  @AfterReturning(pointcut = "execution(* pe.edu.cibertec.eva.service.TaskService.updateStatus(..)) && args(id, status, user)",
                  returning = "result")
  public void logUpdate(Long id, Status status, User user, Object result){
    log.info("TASK-UPDATE by={} id={} newStatus={}", user.getUsername(), id, status);
  }
}
