package pe.edu.cibertec.eva.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttribute;
import pe.edu.cibertec.eva.entity.UserEntity;

import jakarta.servlet.http.HttpServletRequest;
import pe.edu.cibertec.eva.util.Constants;

@ControllerAdvice
public class GlobalModel {

    @ModelAttribute
    public void common(Model model,
                       HttpServletRequest req,
                       @SessionAttribute(value = "user", required = false) UserEntity user) {
        model.addAttribute("ctx", req.getContextPath());
        boolean isAdmin = user != null && user.getRole().equalsIgnoreCase(Constants.ATRIBUT_ADMIN);
        model.addAttribute("user", user);
        model.addAttribute("isAdmin", isAdmin);
    }
}
