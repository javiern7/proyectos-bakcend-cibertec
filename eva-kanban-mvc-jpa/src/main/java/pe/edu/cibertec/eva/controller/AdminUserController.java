package pe.edu.cibertec.eva.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.eva.entity.User;
import pe.edu.cibertec.eva.service.UserService;


@Controller
@RequestMapping("/admin/users")
@SessionAttributes("user")
public class AdminUserController {

    private final UserService userService;
    public AdminUserController(UserService userService) { this.userService = userService; }

    private boolean isAdmin(User u) {
        return u != null && u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole());
    }

    @GetMapping
    public String list(@SessionAttribute("user") User current, Model model) {
        if (!isAdmin(current)) return "redirect:/access-denied";
        model.addAttribute("user", current); // navbar
        model.addAttribute("pageTitle", "Usuarios");
        model.addAttribute("users", userService.findAll());
        return "users"; // crea users.html (tabla simple)
    }

    @GetMapping("/new")
    public String newUser(@SessionAttribute("user") User current, Model model) {
        if (!isAdmin(current)) return "redirect:/access-denied";
        model.addAttribute("user", current);
        model.addAttribute("pageTitle", "Nuevo Usuario");
        model.addAttribute("userForm", new User()); // objeto del form
        return "new-user";
    }

    @PostMapping("/create")
    public String create(@SessionAttribute("user") User current,
                         @ModelAttribute("userForm") User userForm) {
        if (!isAdmin(current)) return "redirect:/access-denied";
        userService.create(userForm);
        return "redirect:/admin/users?created=1";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @SessionAttribute("user") User current,
                       Model model) {
        if (!isAdmin(current)) return "redirect:/access-denied";
        model.addAttribute("user", current);
        model.addAttribute("pageTitle", "Editar Usuario");
        model.addAttribute("userForm", userService.findById(id));
        return "edit-user";
    }

    @PostMapping("/update")
    public String update(@SessionAttribute("user") User current,
                         @ModelAttribute("userForm") User userForm) {
        if (!isAdmin(current)) return "redirect:/access-denied";
        userService.update(userForm);
        return "redirect:/admin/users?updated=1";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @SessionAttribute("user") User current) {
        if (!isAdmin(current)) return "redirect:/access-denied";
        userService.delete(id);
        return "redirect:/admin/users?deleted=1";
    }
}
