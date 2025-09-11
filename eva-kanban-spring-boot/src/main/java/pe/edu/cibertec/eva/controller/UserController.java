package pe.edu.cibertec.eva.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import pe.edu.cibertec.eva.entity.UserEntity;
import pe.edu.cibertec.eva.service.TaskService;
import pe.edu.cibertec.eva.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@SessionAttributes("user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/login"})
    public String loginPage() {
        log.info("Auth: open login form");
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(HttpServletRequest request,
                          @RequestParam String username,
                          @RequestParam String password,
                          Model model,
                          SessionStatus status) {
        log.info("Auth: login attempt user={}", username);
        UserEntity user = userService.login(username, password);

        if (user == null || !user.getEnabled()) {
            model.addAttribute("error", "Usuario o contraseña inválidos");
            return "login";
        }

        // 👉 Guarda el usuario en sesión para que @SessionAttribute lo encuentre en /board
        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);

        // No pases isAdmin por query ni en el modelo aquí.
        log.info("Auth: login success user={}", username);
        return "redirect:/board";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, SessionStatus status) {
        log.info("Auth: logout user");
        status.setComplete();
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return "redirect:/login";
    }
}
