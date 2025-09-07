package pe.edu.cibertec.eva.service;

import java.util.Map;

public interface MetricsService {
    Map<String,Object> weeklyForUser(Long userId, int days);
}
