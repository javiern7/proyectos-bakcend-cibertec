package pe.edu.cibertec.eva.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.eva.entity.Status;
import pe.edu.cibertec.eva.entity.Task;
import pe.edu.cibertec.eva.entity.User;
import pe.edu.cibertec.eva.service.TaskService;
import pe.edu.cibertec.eva.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@SessionAttributes("user")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    private boolean isAdmin(User u) { return u != null && u.getRole().equalsIgnoreCase("ADMIN"); }

    // disponible para todas las vistas de este controller (combo de estados)
    @ModelAttribute("statuses")
    public List<Status> statuses() {
        return Arrays.asList(Status.ASSIGNED, Status.IN_PROGRESS, Status.DONE);
    }

    @GetMapping({"/", "/board"})
    public String board(@SessionAttribute(value = "user", required = false) User user,
                        Model model) {
        if (user == null) return "redirect:/login";

        // Datos base
        model.addAttribute("user", user);
        model.addAttribute("currentUsername", user.getUsername());
        boolean isAdmin = user.getRole() != null && user.getRole().equalsIgnoreCase("ADMIN");
        model.addAttribute("isAdmin", isAdmin);

        // Tareas visibles para el usuario
        List<Task> tasks = taskService.findAllFor(user);
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
        model.addAttribute("newTask", new Task());
        model.addAttribute("statuses", Status.values());

        model.addAttribute("pageTitle", "Tablero de Tareas");
        model.addAttribute("isAdmin", user.getRole().equals("ADMIN"));

        return "board";
    }

    /*@GetMapping("/task/new")
    public String newTask(@SessionAttribute("user") User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Nueva Tarea");
        model.addAttribute("isAdmin", user.getRole().equals("ADMIN"));

        Task t = new Task();
        t.setStatus(Status.ASSIGNED);
        model.addAttribute("task", t);

        model.addAttribute("assignees", userService.findAll());
        model.addAttribute("statuses", Status.values());
        return "new-task";
    }*/

    @GetMapping("/task/new")
    public String newTask(@SessionAttribute("user") User current, Model model) {
        model.addAttribute("user", current);
        model.addAttribute("pageTitle", "Nueva Tarea");
        model.addAttribute("task", new Task());
        model.addAttribute("assignees", userService.findAll());
        model.addAttribute("owners", userService.findAll());
        return "new-task";
    }

    /*@PostMapping("/task/create")
    public String create(@ModelAttribute("task") Task task,
                         @RequestParam(value = "ownerId", required = false) Long ownerId,
                         @SessionAttribute("user") User current) {
        taskService.create(task, current, ownerId); // <-- nuevo método con ownerId
        return "redirect:/board?created=1";
    }*/

    @PostMapping("/task/create")
    public String create(@ModelAttribute("task") Task task,
                         @RequestParam(value = "ownerId", required = false) Long ownerId,
                         @RequestParam(value = "assigneeId", required = false) Long assigneeId,
                         @SessionAttribute("user") User current) {
        taskService.create(task, current, ownerId, assigneeId);
        return "redirect:/board?saved=1";
    }

    /*@GetMapping("/task/edit/{id}")
    public String editTask(@PathVariable Long id,
                           @SessionAttribute("user") User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Editar Tarea");
        model.addAttribute("isAdmin", user.getRole().equals("ADMIN"));
        model.addAttribute("task", taskService.findById(id));
        model.addAttribute("assignees", userService.findAll());
        model.addAttribute("statuses", Status.values());
        return "edit-task";
    }*/

    @GetMapping("/task/edit/{id}")
    public String edit(@PathVariable Long id,
                       @SessionAttribute("user") User user,
                       Model model) {
        Task t = taskService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Editar Tarea");
        model.addAttribute("task", t);
        // lista de posibles asignatarios: si ADMIN todos, si USER solo él
        model.addAttribute("assignees", isAdmin(user) ? userService.findAll() : List.of(user));
        return "edit-task"; // tu vista
    }

    /*@PostMapping("/task/update")
    public String update(@ModelAttribute("task") Task task,
                         @RequestParam(value = "ownerId", required = false) Long ownerId,
                         @SessionAttribute("user") User current) {
        taskService.update(task, current, ownerId); // <-- idem
        return "redirect:/board?updated=1";
    }*/

    @PostMapping("/task/update")
    public String update(@ModelAttribute("task") Task form,
                         @RequestParam(required = false) Long ownerId,
                         @RequestParam(required = false) Long assigneeId,
                         @SessionAttribute("user") User actor) {
        taskService.update(form, actor, ownerId, assigneeId);
        return "redirect:/board?updated=1";
    }

    // Botón o AJAX para cambiar estado
    /*@PostMapping("/task/{id}/status")
    public Object updateStatus(@PathVariable Long id,
                               @RequestParam Status status,
                               @SessionAttribute("user") User actor,
                               @RequestHeader(value = "X-Requested-With", required = false) String xrw) {
        taskService.updateStatus(id, status, actor); // registra auditoría STATUS_CHANGE con actor

        boolean ajax = "XMLHttpRequest".equalsIgnoreCase(xrw);
        return ajax ? new ResponseEntity<>("OK", HttpStatus.OK)
                : "redirect:/board?updated=1";
    }*/

    @PostMapping(value = "/task/{id}/status", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public ResponseEntity<?> changeStatus(@PathVariable Long id,
                                          @RequestParam("status") Status status,
                                          @SessionAttribute("user") User actor,
                                          @RequestHeader(value = "X-Requested-With", required = false) String xhr,
                                          HttpServletRequest request) {
        try {
            taskService.updateStatus(id, status, actor);

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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("No se pudo actualizar el estado");
        }
    }

    @GetMapping("/task/{id}/status")
    public String statusGuard(HttpServletRequest request) {
        return "redirect:" + request.getContextPath() + "/board";
    }

    @PostMapping("/task/{id}/reassign")
    public String reassign(@PathVariable Long id,
                           @RequestParam String username,
                           @SessionAttribute("user") User actor) {
        taskService.reassign(id, username, actor); // auditoría UPDATE con details "reassign ..."
        return "redirect:/board?updated=1";
    }

    @PostMapping("/task/{id}/delete")
    public String delete(@PathVariable Long id,
                         @SessionAttribute("user") User actor) {
        if (!isAdmin(actor)) return "redirect:/access-denied";
        taskService.delete(id, actor);
        return "redirect:/board?deleted=1";
    }

    @ModelAttribute("assignees")
    public List<User> assignees() {
        return userService.findAll();
    }

}
