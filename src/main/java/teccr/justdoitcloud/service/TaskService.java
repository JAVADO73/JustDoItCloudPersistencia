
package teccr.justdoitcloud.service;

import org.springframework.stereotype.Service;
import teccr.justdoitcloud.data.Task;
import teccr.justdoitcloud.data.User;
import teccr.justdoitcloud.repository.TaskRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getTasksForUser(User user) {
        return taskRepository.findByUserId(user.getId());
    }

    public void addTaskToUser(User user, Task task) {
        task.setUserId(user.getId());
        taskRepository.save(task);
    }

    // Avanzar una tarea al siguiente estado
    public void advanceTask(Long taskId, User user) {

        Optional<Task> optionalTask = taskRepository.findById(taskId);

        if (optionalTask.isEmpty()) {
            throw new IllegalArgumentException("La tarea no existe");
        }

        Task task = optionalTask.get();

        // Comprobar que la tarea pertenece al usuario actual
        if (!task.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "No tienes permiso para modificar esta tarea"
            );
        }

        // Cambiar al siguiente estado
        Task.Status nextStatus;

        switch (task.getStatus()) {

            case PENDING:
                nextStatus = Task.Status.INPROGRESS;
                break;

            case INPROGRESS:
                nextStatus = Task.Status.DONE;
                break;

            case DONE:
                return;

            default:
                return;
        }

        // Crear una nueva instancia con el estado actualizado
        Task updatedTask = new Task(
                task.getId(),
                task.getDescription(),
                task.getCreatedAt(),
                task.getDeadline(),
                nextStatus
        );

        updatedTask.setUserId(task.getUserId());

        // Guardar cambios en la base de datos
        taskRepository.save(updatedTask);
    }
}
