package pe.edu.cibertec.eva.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import pe.edu.cibertec.eva.entity.UserEntity;

@Controller
public class ErrorPageController {

    @GetMapping("/access-denied")
    public String accessDenied(@SessionAttribute(value = "user", required = false) UserEntity user,
                               Model model) {
        model.addAttribute("user", user); // para navbar/rol en layout
        model.addAttribute("pageTitle", "Acceso denegado");
        return "access-denied";
    }
}