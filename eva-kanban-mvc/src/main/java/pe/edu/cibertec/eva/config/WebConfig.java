package pe.edu.cibertec.eva.config;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pe.edu.cibertec.eva.interceptor.AuthInterceptor;

public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login", "/doLogin", "/logout",
                        "/error", "/favicon.ico",
                        "/css/**", "/js/**", "/images/**", "/webjars/**", "/resources/**", "/static/**"
                );
    }
}
