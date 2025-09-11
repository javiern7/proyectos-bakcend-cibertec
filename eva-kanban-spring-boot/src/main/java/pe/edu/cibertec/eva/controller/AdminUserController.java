package pe.edu.cibertec.eva.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.eva.entity.UserEntity;
import pe.edu.cibertec.eva.service.UserService;
import pe.edu.cibertec.eva.util.Constants;


@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    private final UserService userService;
    public AdminUserController(UserService userService) { this.userService = userService; }

    private boolean isAdmin(UserEntity u) {
        return u != null && u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole());
    }

    @GetMapping
    public String list(@SessionAttribute("user") UserEntity current, Model model) {
        log.info("Admin: list users");
        if (!isAdmin(current)) return Constants.REDIRECT_ACCESS_DENIED;
        model.addAttribute("user", current); // navbar
        model.addAttribute(Constants.ATRIBUT_TITLE, "Usuarios");
        model.addAttribute("users", userService.findAll());
        return "users"; // crea users.html (tabla simple)
    }

    @GetMapping("/new")
    public String newUser(@SessionAttribute("user") UserEntity current, Model model) {
        log.info("Admin: open create-user form");
        if (!isAdmin(current)) return Constants.REDIRECT_ACCESS_DENIED;
        model.addAttribute("user", current);
        model.addAttribute(Constants.ATRIBUT_TITLE, "Nuevo Usuario");
        model.addAttribute("userForm", new UserEntity()); // objeto del form
        return "new-user";
    }

    @PostMapping("/create")
    public String create(@SessionAttribute("user") UserEntity current,
                         @ModelAttribute("userForm") UserEntity userForm) {
        log.info("Admin: save user username={} by={}", current.getUsername(), userForm.getUsername());
            if (!isAdmin(current)) return Constants.REDIRECT_ACCESS_DENIED;
            userService.create(userForm);
            return "redirect:/admin/users?created=1";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @SessionAttribute("user") UserEntity current,
                       Model model) {
        log.info("Admin: open edit-user form");
        if (!isAdmin(current)) return Constants.REDIRECT_ACCESS_DENIED;
        model.addAttribute("user", current);
        model.addAttribute(Constants.ATRIBUT_TITLE, "Editar Usuario");
        model.addAttribute("userForm", userService.findById(id));
        return "edit-user";
    }

    @PostMapping("/update")
    public String update(@SessionAttribute("user") UserEntity current,
                         @ModelAttribute("userForm") UserEntity userForm) {
        log.info("Admin: open update-user form");
        if (!isAdmin(current)) return Constants.REDIRECT_ACCESS_DENIED;
        userService.update(userForm);
        return "redirect:/admin/users?updated=1";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @SessionAttribute("user") UserEntity current) {
        log.warn("Admin: delete user id={} by={}", id, current.getUsername());
            if (!isAdmin(current)) return Constants.REDIRECT_ACCESS_DENIED;
            userService.delete(id);
            log.info("Admin: user deleted id={}", id);
            return "redirect:/admin/users?deleted=1";

    }
}
