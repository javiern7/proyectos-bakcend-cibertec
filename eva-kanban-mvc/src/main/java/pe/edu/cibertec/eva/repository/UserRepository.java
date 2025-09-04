package pe.edu.cibertec.eva.repository;

import pe.edu.cibertec.eva.dto.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    List<User> findAll();
    Optional<User> findById(Long id);
    User save(User user);   // create
    User update(User user); // update
    void deleteById(Long id);
}