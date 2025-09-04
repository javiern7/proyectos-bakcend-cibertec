package pe.edu.cibertec.eva.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.eva.dto.Role;
import pe.edu.cibertec.eva.dto.Status;
import pe.edu.cibertec.eva.dto.Task;
import pe.edu.cibertec.eva.dto.User;
import pe.edu.cibertec.eva.service.TaskService;
import pe.edu.cibertec.eva.service.UserService;

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

    private boolean isAdmin(User u) { return u != null && u.getRole() == Role.ADMIN; }

    // disponible para todas las vistas de este controller (combo de estados)
    @ModelAttribute("statuses")
    public List<Status> statuses() {
        return Arrays.asList(Status.ASSIGNED, Status.IN_PROGRESS, Status.DONE);
    }

    @GetMapping("/board")
    public String board(@SessionAttribute("user") User user, Model model) {
        // usuario para navbar/saludo
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Tablero de Tareas");
        // 1) Tareas
        List<Task> tasks = taskService.findAllFor(user);
        model.addAttribute("tasks", tasks);
        // 2) Métricas
        Map<String, Long> metrics = taskService.metricsFor(user);
        model.addAttribute("metrics", metrics);
        // 3) Form "Crear nueva tarea" al pie del tablero
        model.addAttribute("newTask", new Task());
        model.addAttribute("assignees", userService.findAll());
        return "board";
    }

    @GetMapping("/task/new")
    public String newTask(@SessionAttribute("user") User user, Model model) {
        model.addAttribute("user", user);              // navbar
        model.addAttribute("pageTitle", "Nueva Tarea");
        model.addAttribute("task", new Task());        // objeto del form de new-task.html
        model.addAttribute("assignees", userService.findAll());
        return "new-task";
    }

    @PostMapping("/task/create")
    public String create(Task newTask,
                         @SessionAttribute("user") User current) {
        taskService.create(newTask, current);
        return "redirect:/board?created=1";
    }

    @GetMapping("/task/edit/{id}")
    public String edit(@PathVariable Long id,
                       @SessionAttribute("user") User user,
                       Model model) {
        if (!isAdmin(user)) return "redirect:/access-denied";
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Editar Tarea");
        model.addAttribute("task", taskService.findById(id));
        model.addAttribute("assignees", userService.findAll());
        return "edit-task";
    }

    @PostMapping("/task/update")
    public String update(@ModelAttribute("task") Task task,@SessionAttribute("user") User user) {
        if (!isAdmin(user)) return "redirect:/access-denied";
        taskService.update(task);
        return "redirect:/board?updated=1";
    }

    // Opcionales (si necesitas acciones rápidas desde botones)
    @PostMapping("/task/{id}/status")
    public Object updateStatus(@PathVariable Long id,
                               @RequestParam Status status,
                               @SessionAttribute("user") User user,
                               @RequestHeader(value="X-Requested-With", required=false) String xrw) {
        Task t = taskService.findById(id);
        boolean isAdmin = user != null && user.getRole() != null && user.getRole().toString().equals("ADMIN");
        boolean isOwner = user != null && t.getUsername() != null && t.getUsername().equals(user.getUsername());

        if (!isAdmin) {
            // solo dueño y solo hacia adelante
            Status curr = t.getStatus();
            Status next = (curr == Status.ASSIGNED) ? Status.IN_PROGRESS :
                    (curr == Status.IN_PROGRESS) ? Status.DONE : null;
            boolean forwardAllowed = (next != null && next == status);
            if (!(isOwner && forwardAllowed)) {
                return (xrw != null) ? new ResponseEntity<>("FORBIDDEN", HttpStatus.FORBIDDEN)
                        : "redirect:/access-denied";
            }
        }

        taskService.updateStatus(id, status);
        if ("XMLHttpRequest".equalsIgnoreCase(xrw)) {
            return new ResponseEntity<>("OK", HttpStatus.OK);
        }
        return "redirect:/board?updated=1";
    }

    @PostMapping("/task/{id}/reassign")
    public String reassign(@PathVariable Long id, @RequestParam String username) {
        taskService.reassign(id, username);
        return "redirect:/board?updated=1";
    }

    @PostMapping("/task/{id}/delete")
    public String delete(@PathVariable Long id, @SessionAttribute("user") User user) {
        if (!isAdmin(user)) return "redirect:/access-denied";
        taskService.delete(id);
        return "redirect:/board?deleted=1";
    }

    @ModelAttribute("assignees")
    public List<User> assignees() {
        return userService.findAll();
    }


}
