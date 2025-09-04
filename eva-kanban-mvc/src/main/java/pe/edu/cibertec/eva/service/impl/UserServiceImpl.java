package pe.edu.cibertec.eva.service.impl;

import org.springframework.stereotype.Service;
import pe.edu.cibertec.eva.dto.User;
import pe.edu.cibertec.eva.repository.UserRepository;
import pe.edu.cibertec.eva.service.UserService;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repo;

    public UserServiceImpl(UserRepository repo) { this.repo = repo; }

    @Override public User login(String username, String password) {
        return repo.findByUsername(username)
                .filter(u -> u.getPassword().equals(password))
                .orElse(null);
    }
    @Override public List<User> findAll() { return repo.findAll(); }
    @Override public User findById(Long id) { return repo.findById(id).orElseThrow(); }
    @Override public User create(User user) { return repo.save(user); }
    @Override public User update(User user) { return repo.update(user); }
    @Override public void delete(Long id) { repo.deleteById(id); }
}
