package pe.edu.cibertec.eva.service;

import pe.edu.cibertec.eva.entity.UserEntity;
import java.util.List;

public interface UserService {
    UserEntity login(String username, String password); // null si falla
    List<UserEntity> findAll();
    UserEntity findById(Long id);
    UserEntity create(UserEntity user);
    UserEntity update(UserEntity user);
    void delete(Long id);
}
