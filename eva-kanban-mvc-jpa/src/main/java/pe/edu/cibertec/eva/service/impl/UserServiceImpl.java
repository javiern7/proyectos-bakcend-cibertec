package pe.edu.cibertec.eva.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.eva.dto.User;
import pe.edu.cibertec.eva.repository.UserRepository;
import pe.edu.cibertec.eva.service.UserService;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    public UserServiceImpl(UserRepository userRepository) { this.userRepository = userRepository; }

    @Transactional(readOnly = true)
    @Override
    public User login(String username, String rawPassword) {
        if (username == null || rawPassword == null) return null;

        String u = username.trim().toLowerCase(Locale.ROOT);
        String p = rawPassword; // si luego encriptas, usa passwordEncoder.matches

        return userRepository.findByUsername(u)
                .filter(user -> Boolean.TRUE.equals(user.getEnabled()))
                .filter(db -> Objects.equals(db.getPassword(), p))
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() { return userRepository.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) { return userRepository.findById(id).orElse(null); }

    @Override
    @Transactional
    public User create(User u) { u.setId(null); return userRepository.save(u); }

    @Override
    @Transactional
    public User update(User u) {
        User e = userRepository.findById(u.getId()).orElseThrow();
        e.setUsername(u.getUsername());
        e.setPassword(u.getPassword());
        e.setRole(u.getRole());
        e.setEnabled(Boolean.TRUE.equals(u.getEnabled()));
        return userRepository.save(e);
    }

    @Override
    @Transactional
    public void delete(Long id) { userRepository.deleteById(id); }

}
