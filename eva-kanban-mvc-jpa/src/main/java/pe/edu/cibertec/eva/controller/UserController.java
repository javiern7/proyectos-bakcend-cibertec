package pe.edu.cibertec.eva.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import pe.edu.cibertec.eva.dto.User;
import pe.edu.cibertec.eva.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@SessionAttributes("user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping({"/login"})
    public String loginPage() {
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(HttpServletRequest request,
                          @RequestParam String username,
                          @RequestParam String password,
                          Model model,
                          SessionStatus status) {

        User user = userService.login(username, password);

        if (user == null || !user.getEnabled()) {
            model.addAttribute("error", "Usuario o contraseña inválidos");
            return "login";
        }

        // 👉 Guarda el usuario en sesión para que @SessionAttribute lo encuentre en /board
        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);

        // No pases isAdmin por query ni en el modelo aquí.
        return "redirect:/board";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, SessionStatus status) {
        status.setComplete();
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return "redirect:/login";
    }
}
