package pe.edu.cibertec.eva.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.eva.entity.Status;
import pe.edu.cibertec.eva.entity.TaskEntity;
import pe.edu.cibertec.eva.entity.UserEntity;
import pe.edu.cibertec.eva.service.TaskService;
import pe.edu.cibertec.eva.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import pe.edu.cibertec.eva.util.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    private boolean isAdmin(UserEntity u) { return u != null && u.getRole().equalsIgnoreCase(Constants.ATRIBUT_ADMIN); }

    // disponible para todas las vistas de este controller (combo de estados)
    @ModelAttribute("statuses")
    public List<Status> statuses() {
        return Arrays.asList(Status.ASSIGNED, Status.IN_PROGRESS, Status.DONE);
    }

    @GetMapping({"/", "/board"})
    public String board(@SessionAttribute(value = "user", required = false) UserEntity user,
                        Model model) {
        log.info("Board: Iniciando panel");
        if (user == null) return "redirect:/login";

        // Datos base
        model.addAttribute("user", user);
        model.addAttribute("currentUsername", user.getUsername());
        boolean isAdmin = user.getRole() != null && user.getRole().equalsIgnoreCase(Constants.ATRIBUT_ADMIN);
        model.addAttribute("isAdmin", isAdmin);

        // Tareas visibles para el usuario
        List<TaskEntity> tasks = taskService.findAllFor(user);
        model.addAttribute("tasks", tasks);

        // Listas ya filtradas por estado (evita th:if complicados)
        model.addAttribute("assignedTasks",
                tasks.stream().filter(t -> t.getStatus() == Status.ASSIGNED).toList());
        model.addAttribute("inProgressTasks",
                tasks.stream().filter(t -> t.getStatus() == Status.IN_PROGRESS).toList());
        model.addAttribute("doneTasks",
                tasks.stream().filter(t -> t.getStatus() == Status.DONE).toList());

        // Métricas por usuario
        Map<String, Long> metrics = taskService.metricsFor(user);
        model.addAttribute("metrics", metrics);
        // Si tu HTML aún espera estos nombres, los exponemos también:
        model.addAttribute("countAssigned",   metrics.getOrDefault("ASSIGNED", 0L));
        model.addAttribute("countInProgress", metrics.getOrDefault("IN_PROGRESS", 0L));
        model.addAttribute("countDone",       metrics.getOrDefault("DONE", 0L));

        // Soporte para formulario "Nueva tarea" (si lo usas en board)
        model.addAttribute("newTask", new TaskEntity());
        model.addAttribute("statuses", Status.values());

        model.addAttribute(Constants.ATRIBUT_TITLE, "Tablero de Tareas");
        model.addAttribute("isAdmin", user.getRole().equals(Constants.ATRIBUT_ADMIN));

        return "board";
    }

    @GetMapping("/task/new")
    public String newTask(@SessionAttribute("user") UserEntity current, Model model) {
        log.info("Create: new user={}", current.getUsername());
        model.addAttribute("user", current);
        model.addAttribute(Constants.ATRIBUT_TITLE, "Nueva Tarea");
        model.addAttribute("task", new TaskEntity());
        model.addAttribute("assignees", userService.findAll());
        model.addAttribute("owners", userService.findAll());
        return "new-task";
    }

    @PostMapping("/task/create")
    public String create(@ModelAttribute("task") TaskEntity task,
                         @RequestParam(value = "ownerId", required = false) Long ownerId,
                         @RequestParam(value = "assigneeId", required = false) Long assigneeId,
                         @SessionAttribute("user") UserEntity current) {
        log.info("Task: save title='{}' assignee={} by={}",
                task.getTitle(),
                task.getAssignedTo()!=null?task.getAssignedTo().getUsername():"-",
                current.getUsername());
            taskService.create(task, current, ownerId, assigneeId);
            log.info("Task: saved id={} status={}", task.getId(), task.getStatus());
            return "redirect:/board?saved=1";
    }

    @GetMapping("/task/edit/{id}")
    public String edit(@PathVariable Long id,
                       @SessionAttribute("user") UserEntity user,
                       Model model) {
        log.info("Task: open edit id={} by={}", id, user.getUsername());
        TaskEntity t = taskService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute(Constants.ATRIBUT_TITLE, "Editar Tarea");
        model.addAttribute("task", t);
        // lista de posibles asignatarios: si ADMIN todos, si USER solo él
        model.addAttribute("assignees", isAdmin(user) ? userService.findAll() : List.of(user));
        return "edit-task"; // tu vista
    }

    @PostMapping("/task/update")
    public String update(@ModelAttribute("task") TaskEntity form,
                         @RequestParam(required = false) Long ownerId,
                         @RequestParam(required = false) Long assigneeId,
                         @SessionAttribute("user") UserEntity actor) {
        taskService.update(form, actor, ownerId, assigneeId);
        return "redirect:/board?updated=1";
    }

    @PostMapping(value = "/task/{id}/status", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public ResponseEntity<String> changeStatus(@PathVariable Long id,
                                          @RequestParam("status") Status status,
                                          @SessionAttribute("user") UserEntity actor,
                                          @RequestHeader(value = "X-Requested-With", required = false) String xhr,
                                          HttpServletRequest request) {
        final String username = (actor != null && actor.getUsername() != null) ? actor.getUsername() : "anonymous";
        log.info("Task: change status id={} -> {} by={}", id, status, username);
        try {
            taskService.updateStatus(id, status, actor);
            log.info("Task: status updated id={} new={}", id, status);
            // AJAX (nuestro fetch envía este header)
            if ("XMLHttpRequest".equalsIgnoreCase(xhr)) {
                return ResponseEntity.ok().build();
            }

            // NO-AJAX: redirige con context path
            String ctx = request.getContextPath(); // ej: /eva_kanban_mvc_jpa
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, ctx + "/board?saved=1")
                    .build();

        } catch (IllegalStateException e) {
            log.warn("Task: invalid state change id={} new={} by={} msg={}", id, status, username, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Task: not found id={} msg={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Task: unexpected error changing status id={} by={}", id, username, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("No se pudo actualizar el estado");
        }
    }

    @GetMapping("/task/{id}/status")
    public String statusGuard(@PathVariable Long id,HttpServletRequest request) {
        return "redirect:" + request.getContextPath() + "/board";
    }

    @PostMapping("/task/{id}/reassign")
    public String reassign(@PathVariable Long id,
                           @RequestParam String username,
                           @SessionAttribute("user") UserEntity actor) {
        taskService.reassign(id, username, actor); // auditoría UPDATE con details "reassign ..."
        return "redirect:/board?updated=1";
    }

    @PostMapping("/task/{id}/delete")
    public String delete(@PathVariable Long id,
                         @SessionAttribute("user") UserEntity actor) {
        if (!isAdmin(actor)) return "redirect:/access-denied";
        taskService.delete(id, actor);
        return "redirect:/board?deleted=1";
    }

    @ModelAttribute("assignees")
    public List<UserEntity> assignees() {
        return userService.findAll();
    }

}
