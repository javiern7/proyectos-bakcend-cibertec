package pe.edu.cibertec.eva.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if (req.getRequestURI().startsWith("/login") || req.getRequestURI().startsWith("/css")
                || req.getRequestURI().startsWith("/js") || req.getRequestURI().startsWith("/images")) {
            return true;
        }
        Object user = req.getSession(false) != null ? req.getSession(false).getAttribute("user") : null;
        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/login?required=1");
            return false;
        }
        return true;
    }
}
