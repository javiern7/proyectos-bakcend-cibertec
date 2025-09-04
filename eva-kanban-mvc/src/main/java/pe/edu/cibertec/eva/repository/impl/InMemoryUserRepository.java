package pe.edu.cibertec.eva.repository.impl;

import org.springframework.stereotype.Repository;
import pe.edu.cibertec.eva.dto.Role;
import pe.edu.cibertec.eva.dto.User;
import pe.edu.cibertec.eva.repository.UserRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> data = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    public InMemoryUserRepository() {
        // seeds
        add("admin",  "admin",     Role.ADMIN, "admin@local");
        add("javier", "javier123", Role.USER,  "javier@local");
        add("maria",  "maria123",  Role.USER,  "maria@local");
    }

    private void add(String username, String password, Role role, String email) {
        User u = new User();
        u.setId(seq.incrementAndGet());
        u.setUsername(username);
        u.setPassword(password);
        u.setRole(role);
        u.setEmail(email);
        data.put(u.getId(), u);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return data.values().stream().filter(u -> Objects.equals(u.getUsername(), username)).findFirst();
    }
    @Override
    public List<User> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(data.get(id));
    }
    @Override
    public User save(User user) {
        user.setId(seq.incrementAndGet());
        data.put(user.getId(), user);
        return user;
    }
    @Override
    public User update(User user) {
        if (user.getId() == null || !data.containsKey(user.getId())) throw new NoSuchElementException("User no existe");
        data.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteById(Long id) {
        data.remove(id);
    }
}
