package pe.edu.cibertec.eva.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.cibertec.eva.entity.UserEntity;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger accessLog = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Evita ruido en estáticos / health / favicon
        String p = request.getRequestURI();
        return p.startsWith("/css/")
                || p.startsWith("/js/")
                || p.startsWith("/images/")
                || p.startsWith("/webjars/")
                || p.startsWith("/actuator/health")
                || p.endsWith(".ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        final String reqId = UUID.randomUUID().toString();
        final long start = System.currentTimeMillis();

        // Usuario (si está en sesión)
        String username = "-";
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object u = session.getAttribute("user");
            if (u instanceof UserEntity user) {
                username = user.getUsername();
            }
        }

        // Cargar MDC al inicio (disponible para todos los logs de la request)
        MDC.put("reqId",  reqId);
        MDC.put("user",   username);
        MDC.put("method", req.getMethod());
        MDC.put("path",   req.getRequestURI());
        // placeholders (estas dos se completan al final)
        MDC.put("status", "-");
        MDC.put("time",   "-");

        try {
            chain.doFilter(req, res);
        } finally {
            long ms = System.currentTimeMillis() - start;
            MDC.put("status", String.valueOf(res.getStatus()));
            MDC.put("time",   String.valueOf(ms));

            // Línea de access log (una por request)
            accessLog.info("ACCESS {}", req.getRequestURI());

            // Limpieza obligatoria
            MDC.clear();
        }
    }
}