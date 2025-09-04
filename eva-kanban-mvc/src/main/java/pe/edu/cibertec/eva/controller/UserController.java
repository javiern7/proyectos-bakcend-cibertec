package pe.edu.cibertec.eva.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import pe.edu.cibertec.eva.dto.User;
import pe.edu.cibertec.eva.service.UserService;

import javax.servlet.http.HttpSession;

@Controller
@SessionAttributes("user")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping({"/", "/login"})
    public String loginView() {
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          Model model) {
        User u = userService.login(username, password);
        if (u == null) {
            model.addAttribute("error", "Usuario o contraseña inválidos");
            return "login";
        }
        model.addAttribute("user", u); // queda en sesión por @SessionAttributes
        return "redirect:/board";
    }

    @GetMapping("/logout")
    public String logout(SessionStatus status, HttpSession session) {
        status.setComplete();
        if (session != null) session.invalidate();
        return "redirect:/login";
    }
}
