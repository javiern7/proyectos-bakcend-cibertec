package pe.edu.cibertec.eva.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.eva.dto.User;
import pe.edu.cibertec.eva.repository.AuditLogRepository;

@Controller
@RequestMapping("/admin/audit")
@SessionAttributes("user")
public class AuditController {

    private final AuditLogRepository repo;

    public AuditController(AuditLogRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/task/{id}")
    public String byTask(@PathVariable("id") Long taskId,
                         @SessionAttribute("user") User current,
                         Model model) {
        if (current == null || !"ADMIN".equalsIgnoreCase(current.getRole()))
            return "redirect:/access-denied";

        model.addAttribute("user", current);
        model.addAttribute("pageTitle", "Auditoría de Tarea " + taskId);
        model.addAttribute("taskId", taskId);
        model.addAttribute("logs", repo.findByTaskIdOrderByCreatedAtDesc(taskId));
        return "audit-task";
    }
}
