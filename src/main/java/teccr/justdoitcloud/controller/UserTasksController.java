
package teccr.justdoitcloud.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import teccr.justdoitcloud.data.Task;
import teccr.justdoitcloud.data.User;
import teccr.justdoitcloud.service.TaskService;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequestMapping("/user/tasks")
@SessionAttributes("user")
public class UserTasksController {

    private final TaskService taskService;

    public UserTasksController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public String showUserTasks(Model model) {

        model.addAttribute(
                "newTask",
                new Task(
                        null,
                        "",
                        LocalDateTime.now(),
                        null,
                        Task.Status.PENDING
                )
        );

        User user = (User) model.getAttribute("user");

        if (user != null) {
            user.setTasks(taskService.getTasksForUser(user));
        }

        return "usertasks";
    }

    @PostMapping
    public String handleTask(
            @RequestParam(required = false) Long taskId,
            @Valid @ModelAttribute(name = "newTask") Task newTask,
            Errors errors,
            @ModelAttribute("user") User user,
            Model model) {

        // Si recibimos un ID, avanzamos una tarea existente
        if (taskId != null) {

            taskService.advanceTask(taskId, user);

            return "redirect:/user/tasks";
        }

        // Si no recibimos ID, creamos una tarea nueva
        if (errors.hasErrors()) {

            user.setTasks(taskService.getTasksForUser(user));

            return "usertasks";
        }

        log.info("Adding task: " + newTask);

        taskService.addTaskToUser(user, newTask);

        return "redirect:/user/tasks";
    }
}
