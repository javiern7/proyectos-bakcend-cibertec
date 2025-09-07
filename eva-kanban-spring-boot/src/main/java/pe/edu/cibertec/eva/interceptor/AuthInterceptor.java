package pe.edu.cibertec.eva.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import pe.edu.cibertec.eva.entity.UserEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Set;

public class AuthInterceptor implements HandlerInterceptor {
    private static final Set<String> PUBLIC = Set.of("/login", "/doLogin", "/index.html");

    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String path = req.getServletPath();

        if (PUBLIC.contains(path)) return true;
        if (path.startsWith("/resources/") || path.startsWith("/static/") ||
                path.startsWith("/static/css/") || path.startsWith("/js/") ||
                path.startsWith("/images/") || path.startsWith("/webjars/")) {
            return true;
        }

        HttpSession session = req.getSession(false);
        UserEntity u = (session != null) ? (UserEntity) session.getAttribute("user") : null;

        if (u == null) {
            String xhr = req.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equalsIgnoreCase(xhr)) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                res.sendRedirect(req.getContextPath() + "/login");
            }
            return false;
        }
        return true;
    }
}
