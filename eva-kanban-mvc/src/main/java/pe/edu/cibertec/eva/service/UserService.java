package pe.edu.cibertec.eva.service;

import pe.edu.cibertec.eva.dto.User;
import java.util.List;

public interface UserService {
    User login(String username, String password); // retorna null si falla
    List<User> findAll();
    User findById(Long id);
    User create(User user);
    User update(User user);
    void delete(Long id);
}
