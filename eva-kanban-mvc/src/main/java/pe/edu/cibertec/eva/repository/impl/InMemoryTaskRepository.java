package pe.edu.cibertec.eva.repository.impl;

import org.springframework.stereotype.Repository;
import pe.edu.cibertec.eva.dto.Status;
import pe.edu.cibertec.eva.dto.Task;
import pe.edu.cibertec.eva.repository.TaskRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryTaskRepository implements TaskRepository {

    private final Map<Long, Task> data = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    public InMemoryTaskRepository() {
        Task t1 = new Task(); t1.setId(seq.incrementAndGet()); t1.setName("Preparar reporte"); t1.setStatus(Status.ASSIGNED);    t1.setUsername("admin");
        Task t2 = new Task(); t2.setId(seq.incrementAndGet()); t2.setName("Diseñar landing");  t2.setStatus(Status.IN_PROGRESS); t2.setUsername("javier");
        Task t3 = new Task(); t3.setId(seq.incrementAndGet()); t3.setName("Revisión QA");      t3.setStatus(Status.DONE);        t3.setUsername("maria");
        data.put(t1.getId(), t1); data.put(t2.getId(), t2); data.put(t3.getId(), t3);
    }
    @Override
    public List<Task> findAll() {
        return new ArrayList<>(data.values())
                .stream()
                .sorted(Comparator.comparing(Task::getId))
                .toList();
    }

    @Override
    public List<Task> findAllFor(String username) {
        return data.values().stream()
                .filter(t -> Objects.equals(username, t.getUsername()))
                .sorted(Comparator.comparing(Task::getId))
                .toList();
    }

    @Override
    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public Task save(Task task) {
        long id = seq.incrementAndGet();
        task.setId(id);
        data.put(id, task);
        return task;
    }

    @Override
    public Task update(Task task) {
        if (task.getId() == null || !data.containsKey(task.getId())) {
            throw new IllegalArgumentException("Task no existe para update");
        }
        data.put(task.getId(), task);
        return task;
    }

    @Override
    public void deleteById(Long id) {
        data.remove(id);
    }
}
