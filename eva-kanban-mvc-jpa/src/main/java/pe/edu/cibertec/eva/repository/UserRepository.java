package pe.edu.cibertec.eva.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.cibertec.eva.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUsername(String username);

}