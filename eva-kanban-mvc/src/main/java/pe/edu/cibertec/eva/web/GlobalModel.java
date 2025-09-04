package pe.edu.cibertec.eva.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pe.edu.cibertec.eva.dto.Role;
import pe.edu.cibertec.eva.dto.User;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalModel {

    @ModelAttribute
    public void injectGlobals(HttpServletRequest req, Model model) {
        User user = (User) req.getSession().getAttribute("user");
        model.addAttribute("user", user);
        model.addAttribute("ctx", req.getContextPath());

        boolean isAdmin = false;
        if (user != null) {
            // Si usas enum:
            try { isAdmin = user.getRole() == Role.ADMIN; } catch (Throwable ignore) {}
        }
        model.addAttribute("isAdmin", isAdmin);
    }
}
