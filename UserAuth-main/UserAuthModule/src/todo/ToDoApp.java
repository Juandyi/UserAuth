package todo;

import javax.swing.*;

import auth.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ToDoApp {
    private User user;
    private List<String> tasks;
    private Properties props;
    private static final String DATA_FOLDER = "data";
    private static final String FILE_NAME = DATA_FOLDER + File.separator + "todo.properties";

    public ToDoApp(User user) {
        this.user = user;
        this.props = loadProperties();
        this.tasks = loadUserTasks();
    }

    public void start() {
        while (true) {
            String[] options = {"View Tasks", "Add Task", "Delete Task", "Delete All", "Logout"};
            int choice = JOptionPane.showOptionDialog(null,
                    "Welcome, " + user.getUsername() + "! Choose an option:",
                    "Simple To-Do List",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == -1 || choice == 4) {  // Logout or close dialog
                saveUserTasks();
                JOptionPane.showMessageDialog(null, "Goodbye, " + user.getUsername() + "!");
                break;
            }

            switch (choice) {
                case 0:
                    showTasks();
                    break;
                case 1:
                    addTask();
                    break;
                case 2:
                    deleteTask();
                    break;
                case 3:
                    deleteAllTasks();
                    break;
            }
        }
    }

    private void deleteAllTasks() {
        if (tasks.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Your task list is already empty.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to delete ALL your tasks?",
                "Confirm Delete All",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tasks.clear();
            saveUserTasks(); // This will remove all user's tasks from properties file
            JOptionPane.showMessageDialog(null, "All tasks deleted!");
        }
    }


    private void showTasks() {
        if (tasks.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Your task list is empty.");
        } else {
            StringBuilder sb = new StringBuilder("Your Tasks:\n");
            for (int i = 0; i < tasks.size(); i++) {
                sb.append(i + 1).append(". ").append(tasks.get(i)).append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        }
    }

    private void addTask() {
        String task = JOptionPane.showInputDialog("Enter a new task:");
        if (task != null && !task.trim().isEmpty()) {
            tasks.add(task.trim());
            JOptionPane.showMessageDialog(null, "Task added!");
            saveUserTasks();  // Save immediately after adding
        }
    }

    private void deleteTask() {
        if (tasks.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Your task list is empty.");
            return;
        }
        String[] taskArray = tasks.toArray(new String[0]);
        String taskToDelete = (String) JOptionPane.showInputDialog(
                null,
                "Select a task to delete:",
                "Delete Task",
                JOptionPane.PLAIN_MESSAGE,
                null,
                taskArray,
                taskArray[0]);
        if (taskToDelete != null) {
            tasks.remove(taskToDelete);
            JOptionPane.showMessageDialog(null, "Task deleted!");
            saveUserTasks();  // Save after deletion
        }
    }

    // Load properties file from disk
    private Properties loadProperties() {
        Properties p = new Properties();
        File folder = new File(DATA_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();  // Create data folder if it doesn't exist
        }
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                p.load(in);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Failed to load tasks: " + e.getMessage());
            }
        }
        return p;
    }

    // Load tasks for the current user from properties
    private List<String> loadUserTasks() {
        List<String> userTasks = new ArrayList<>();
        int i = 1;
        String key = user.getUsername() + ".task" + i;
        while (props.containsKey(key)) {
            userTasks.add(props.getProperty(key));
            key = user.getUsername() + ".task" + (++i);
        }
        return userTasks;
    }


    // Save current user's tasks into properties and write to file
    private void saveUserTasks() {
        // Remove old tasks for user
        int i = 1;
        String key = user.getUsername() + ".task" + i;
        while (props.containsKey(key)) {
            props.remove(key);
            key = user.getUsername() + ".task" + (++i);
            // key == user005.task4 
        }
        // Add current tasks
        for (i = 0; i < tasks.size(); i++) {
            props.setProperty(user.getUsername() + ".task" + (i + 1), tasks.get(i));
        }
        // Save properties to file
        File folder = new File(DATA_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try (OutputStream out = new FileOutputStream(FILE_NAME)) {
            props.store(out, "User To-Do Lists");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to save tasks: " + e.getMessage());
        }
    }

}
