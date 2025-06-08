package auth;

import javax.swing.*;

import baloto.BalotoApp;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    private List<User> users = new ArrayList<>();
    private List<Admin> admins = new ArrayList<>();

    private static final String DATA_FILE = "data\\userdata.ser";

    public Controller() {
        loadData();
        if (admins.isEmpty()) {
            admins.add(new Admin("admin", "admin123")); // default admin if none loaded
        }
    }

    public void start() {
        while (true) {
            String[] options = {"Admin", "User", "Exit"};
            int choice = JOptionPane.showOptionDialog(null,
                    "Select user type:",
                    "Main Menu",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == -1 || choice == 2) {  // User closed dialog or chose Exit
                saveData();
                System.exit(0);
            }

            switch (choice) {
                case 0: // Admin
                    adminLoginFlow();
                    break;
                case 1: // User
                    userLoginFlow();
                    break;
            }
        }
    SocialCalendarApp.loadMuro();
    }

    private void adminLoginFlow() {
        Admin admin = (Admin) validateUserLogin("admin");  // Your login dialog + validation method
        
        if (admin == null) return;          // User cancelled login    
        JOptionPane.showMessageDialog(null, "Admin authenticated!");

        // Enforce password reset on first login or if flagged
        if (admin.isPasswordResetPending()) {
            boolean changed = promptPasswordChange(admin);
            if (!changed) {
                JOptionPane.showMessageDialog(null, "You must change your password to continue.");
                return;  // Stop login flow until password changed
            }
        }
        adminMenu(admin);
    }

    private Admin findAdmin(String username) {
        for (Admin a : admins) {
            if (a.getUsername().equals(username)) {
                return a;
            }
        }
        return null;
    }

    private void calendarMenu(User user) {
    SocialCalendarApp socialCalendarApp = new SocialCalendarApp(user);
    while (true) {
        String[] options = {
            "Crear evento",
            "Ver mis eventos",
            "Eliminar evento",
            "Publicar en muro",
            "Ver muro social",
            "Cerrar sesión"
        };
        int choice = JOptionPane.showOptionDialog(
                null,
                "Menú de Calendario:",
                "Usuario: " + user.getUsername(),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == -1 || choice == 5) { // Cerrar sesión o cerrar ventana
            // Puedes guardar datos aquí si lo necesitas
            return;
        }

        switch (choice) {
            case 0: // Crear evento
                socialCalendarApp.crearEvento();
                break;
            case 1: // Ver mis eventos
                socialCalendarApp.mostrarEventos();
                break;
            case 2: // Eliminar evento
                socialCalendarApp.eliminarEvento();
                break;
            case 3: // Publicar en muro
                socialCalendarApp.nuevaPublicacion();
                break;
            case 4: // Ver muro social
                socialCalendarApp.mostrarMuro();
                break;
        }
    }
}

    private void adminMenu(Admin admin) {
        while (true) {
            String[] options = {"Create new user", "List users", "Remove user", "New admin", "Logout"};
            int choice = JOptionPane.showOptionDialog(null,
                    "Admin Menu:",
                    "Admin: " + admin.getUsername(),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == -1 || choice == 4) { // Logout or closed dialog
                saveData();
                return;
            }

            switch (choice) {
                case 0: // Create new user
                    User newUser = admin.createNewUser();
                    users.add(newUser);
                    JOptionPane.showMessageDialog(null,
                            "New user created!\nUsername: " + newUser.getUsername() +
                            "\nPassword: " + newUser.getPassword());
                    saveData();
                    break;

                case 1: // List users
                    if (users.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No users registered.");
                    } else {
                        StringBuilder sb = new StringBuilder("Users:\n");
                        for (User u : users) {
                            sb.append(u.getUsername()).append("\n");
                        }
                        JOptionPane.showMessageDialog(null, sb.toString());
                    }
                    break;

                case 2: // Remove user
                    removeUser();
                    break;

                case 3: // New admin
                    createNewAdmin();
                    break;
            }
        }
    }
    
    private void removeUser() {
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No users to remove.");
            return;
        }

        String[] usernames = users.stream()
                .map(User::getUsername)
                .toArray(String[]::new);

        String userToRemove = (String) JOptionPane.showInputDialog(
                null,
                "Select user to remove:",
                "Remove User",
                JOptionPane.PLAIN_MESSAGE,
                null,
                usernames,
                usernames[0]);

        if (userToRemove == null) return; // Cancelled

        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Are you sure you want to remove user '" + userToRemove + "'?",
                "Confirm Remove",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            users.removeIf(u -> u.getUsername().equals(userToRemove));
            saveData();
            JOptionPane.showMessageDialog(null, "User '" + userToRemove + "' removed.");
        }
    }
    
    private void createNewAdmin() {
        // Generate sequential admin username
        String username = "admin" + (admins.size() + 1);

        // Fixed provisional password
        String provisionalPassword = "admin123";

        Admin newAdmin = new Admin(username, provisionalPassword);
        newAdmin.setPasswordResetPending(true); // Force password change on first login
        admins.add(newAdmin);
        saveData();

        JOptionPane.showMessageDialog(null,
                "New admin account created!\nUsername: " + username +
                "\nProvisional password: " + provisionalPassword +
                "\nThe new admin will be required to set a new password on first login.");
    }
    

    private void userLoginFlow() {
        User user = validateUserLogin("user");  // Your login dialog + validation method
        if (user == null) return;          // User cancelled login    
        JOptionPane.showMessageDialog(null, "User authenticated!");

        // Enforce password reset on first login or if flagged
        if (user.isPasswordResetPending()) {
            boolean changed = promptPasswordChange(user);
            if (!changed) {
                JOptionPane.showMessageDialog(null, "You must change your password to continue.");
                return;  // Stop login flow until password changed
            }
        }

        // Launch ToDoApp for authenticated user
        //ToDoApp todoApp = new ToDoApp(user);
        //todoApp.start();
        
        // Launch BalotoApp for authenticated user
        //BalotoApp balotoApp = new BalotoApp(user);
        //balotoApp.start();

        // SocialCalendarApp for authenticated user
        SocialCalendarApp socialCalendarApp = new SocialCalendarApp(user);
        socialCalendarApp.start();
        

        // Save user data here if needed after ToDoApp closes
    }



    private User validateUserLogin(String accountType) {
        while (true) {
            JPanel panel = new JPanel(new GridLayout(2, 2));
            JLabel userLabel = new JLabel("Username:");
            JTextField userField = new JTextField();
            JLabel passLabel = new JLabel("Password:");
            JPasswordField passField = new JPasswordField();
            panel.add(userLabel);
            panel.add(userField);
            panel.add(passLabel);
            panel.add(passField);

            int result = JOptionPane.showConfirmDialog(
                    null, panel, "Login",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) return null; // User cancelled

            String username = userField.getText().trim();
            String password = new String(passField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Username and password cannot be empty.");
                continue;
            }

            User user;
            if ("admin".equalsIgnoreCase(accountType)) {
                user = findAdmin(username);
            } else {
                user = findUser(username);
            }
            if (user != null && user.authenticate(password)) {
                return user; // Successful login
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username or password. Please try again.");
            }
        }
    }

    private boolean promptPasswordChange(User user) {
        while (true) {
            JPanel panel = new JPanel(new GridLayout(2, 2));
            panel.add(new JLabel("New Password:"));
            JPasswordField newPass = new JPasswordField();
            panel.add(newPass);
            panel.add(new JLabel("Confirm Password:"));
            JPasswordField confirmPass = new JPasswordField();
            panel.add(confirmPass);

            int result = JOptionPane.showConfirmDialog(null, panel, "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) return false; // Cancelled

            String newPassword = new String(newPass.getPassword());
            String confirmPassword = new String(confirmPass.getPassword());

            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Password cannot be empty.");
                continue;
            }
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(null, "Passwords do not match. Try again.");
                continue;
            }

            user.setPassword(newPassword);
            user.setPasswordResetPending(false);  // Clear the flag
            // Save user data here if needed
            JOptionPane.showMessageDialog(null, "Password changed successfully!");
            return true;
        }
    }



    private User findUser(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    // Serialization: Save users, admins, and userCounter to file
    private void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(users);
            out.writeObject(admins);
            out.writeInt(Admin.getUserCounter());  // save static counter
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving data: " + e.getMessage());
        }
    }

    // Deserialization: Load users, admins, and userCounter from file
    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            users = (List<User>) in.readObject();
            admins = (List<Admin>) in.readObject();
            int counter = in.readInt();
            Admin.setUserCounter(counter);
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error loading data: " + e.getMessage());
        }
    }
}
       


