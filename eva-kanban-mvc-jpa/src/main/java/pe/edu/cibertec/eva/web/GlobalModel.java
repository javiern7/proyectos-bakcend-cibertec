package pe.edu.cibertec.eva.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pe.edu.cibertec.eva.dto.User;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalModel {

    @ModelAttribute
    public void injectGlobals(HttpServletRequest req, Model model) {
        User user = (User) req.getSession().getAttribute("user");

        // ✅ Ponlos en request (no en model) para que NO aparezcan en la query del redirect
        req.setAttribute("user", user);
        req.setAttribute("ctx", req.getContextPath());

        boolean isAdmin = false;
        if (user != null) {
            try { isAdmin = "ADMIN".equalsIgnoreCase(user.getRole()); } catch (Throwable ignore) {}
        }
        req.setAttribute("isAdmin", isAdmin);

        // (Opcional) si en alguna vista necesitas sí o sí en Model:
        // model.addAttribute("user", user);
    }
}
