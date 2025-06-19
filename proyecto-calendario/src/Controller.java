import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.security.SecureRandom;



/**
 * Implementación de repositorio con archivos
 */
class FileUserRepository implements UserRepository {
    private static final String DATA_FILE = "data/userdata.ser";

    @Override
    public void save(List<User> users, List<Admin> admins, int userCounter) {
        try {
            // Crear directorio data si no existe
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
                out.writeObject(users);
                out.writeObject(admins);
                out.writeInt(userCounter);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving data: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public UserData load() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return new UserData(new ArrayList<>(), new ArrayList<>(), 1);
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            List<User> users = (List<User>) in.readObject();
            List<Admin> admins = (List<Admin>) in.readObject();
            int userCounter = in.readInt();
            return new UserData(users, admins, userCounter);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error loading data: " + e.getMessage());
        }
    }
}

/**
 * Generador de credenciales secuencial
 */
class SequentialCredentialGenerator implements CredentialGenerator {
    private int userCounter;
    private final SecureRandom random = new SecureRandom();

    public SequentialCredentialGenerator(int initialCounter) {
        this.userCounter = initialCounter;
    }

    @Override
    public String generateUsername() {
        return String.format("user%03d", userCounter++);
    }

    @Override
    public String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public int getUserCounter() {
        return userCounter;
    }
}

/**
 * Servicio de autenticación básico
 */
class BasicAuthenticationService implements AuthenticationService {
    private final List<User> users;
    private final List<Admin> admins;

    public BasicAuthenticationService(List<User> users, List<Admin> admins) {
        this.users = users;
        this.admins = admins;
    }

    @Override
    public User authenticate(String username, String password, String userType) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return null;
        }

        List<? extends User> targetList = "admin".equalsIgnoreCase(userType) ? admins : users;
        
        return targetList.stream()
                .filter(user -> user.getUsername().equals(username) && user.authenticate(password))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean changePassword(User user, String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            return false;
        }

        user.setPassword(newPassword);
        user.setPasswordResetPending(false);
        return true;
    }
}

/**
 * Implementación de UI con Swing
 */
class SwingUserInterface implements UserInterface {
    @Override
    public int showMainMenu() {
        String[] options = {"Admin", "User", "Exit"};
        return JOptionPane.showOptionDialog(null,
                "Select user type:",
                "Main Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);
    }

    @Override
    public LoginCredentials showLoginDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        
        panel.add(new JLabel("Username:"));
        panel.add(userField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        return new LoginCredentials(userField.getText().trim(), new String(passField.getPassword()));
    }

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    @Override
    public boolean showConfirmDialog(String message) {
        return JOptionPane.showConfirmDialog(null, message, "Confirm", 
               JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    @Override
    public String showPasswordChangeDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JPasswordField newPass = new JPasswordField();
        JPasswordField confirmPass = new JPasswordField();
        
        panel.add(new JLabel("New Password:"));
        panel.add(newPass);
        panel.add(new JLabel("Confirm Password:"));
        panel.add(confirmPass);

        int result = JOptionPane.showConfirmDialog(null, panel, "Change Password", 
                   JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        String newPassword = new String(newPass.getPassword());
        String confirmPassword = new String(confirmPass.getPassword());
        
        return newPassword.equals(confirmPassword) ? newPassword : null;
    }

    @Override
    public int showAdminMenu(String adminName) {
        String[] options = {"Create new user", "List users", "Remove user", "New admin", "Logout"};
        return JOptionPane.showOptionDialog(null,
                "Admin Menu:",
                "Admin: " + adminName,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);
    }

    @Override
    public String showUserSelectionDialog(String[] usernames) {
        return (String) JOptionPane.showInputDialog(null,
                "Select user to remove:",
                "Remove User",
                JOptionPane.PLAIN_MESSAGE,
                null,
                usernames,
                usernames[0]);
    }
}

/**
 * Controlador principal que maneja toda la lógica de la aplicación
 * Aplica el principio de Inversión de Dependencias usando interfaces
 */
class Controller {
    private final List<User> users;
    private final List<Admin> admins;
    private final UserRepository userRepository;
    private final AuthenticationService authService;
    private final UserInterface ui;
    private final UserManagementService userService;
    private final AdminManagementService adminService;
    private final SequentialCredentialGenerator credentialGenerator;

    /**
     * Constructor por defecto que usa implementaciones concretas
     */
    public Controller() {
        this(new FileUserRepository(), new SwingUserInterface());
    }

    /**
     * Constructor que permite inyección de dependencias
     * @param userRepository repositorio para persistencia de datos
     * @param ui interfaz de usuario
     */
    public Controller(UserRepository userRepository, UserInterface ui) {
        this.userRepository = userRepository;
        this.ui = ui;
        
        // Cargar datos existentes
        UserRepository.UserData data = userRepository.load();
        this.users = data.users;
        this.admins = data.admins;
        
        // Inicializar servicios
        this.credentialGenerator = new SequentialCredentialGenerator(data.userCounter);
        this.authService = new BasicAuthenticationService(users, admins);
        this.userService = new UserManagementService(users, credentialGenerator);
        this.adminService = new AdminManagementService(admins);
        
        // Asegurar que existe al menos un admin por defecto
        adminService.ensureDefaultAdmin();
    }

    /**
     * Inicia la aplicación principal
     */
    public void start() {
        // Cargar datos del muro social
        SocialCalendarApp.loadMuro();
        
        // Bucle principal de la aplicación
        while (true) {
            int choice = ui.showMainMenu();
            
            // Salir de la aplicación
            if (choice == -1 || choice == 2) {
                saveData();
                System.exit(0);
            }

            switch (choice) {
                case 0: // Login como Admin
                    handleAdminLogin();
                    break;
                case 1: // Login como User
                    handleUserLogin();
                    break;
            }
        }
    }

    /**
     * Maneja el proceso de login para administradores
     */
    private void handleAdminLogin() {
        User admin = performLogin("admin");
        if (admin == null) return;

        ui.showMessage("Admin authenticated!");

        // Verificar si necesita cambiar contraseña
        if (admin.isPasswordResetPending()) {
            if (!handlePasswordReset(admin)) {
                ui.showMessage("You must change your password to continue.");
                return;
            }
        }
        
        // Mostrar menú de administrador
        showAdminMenu((Admin) admin);
    }

    /**
     * Maneja el proceso de login para usuarios regulares
     */
    private void handleUserLogin() {
        User user = performLogin("user");
        if (user == null) return;

        ui.showMessage("User authenticated!");

        // Verificar si necesita cambiar contraseña
        if (user.isPasswordResetPending()) {
            if (!handlePasswordReset(user)) {
                ui.showMessage("You must change your password to continue.");
                return;
            }
        }

        // Lanzar aplicación de calendario social
        SocialCalendarApp calendarApp = new SocialCalendarApp(user);
        calendarApp.start();
    }

    /**
     * Realiza el proceso de autenticación
     * @param userType tipo de usuario ("admin" o "user")
     * @return el usuario autenticado o null si falla
     */
    private User performLogin(String userType) {
        while (true) {
            UserInterface.LoginCredentials credentials = ui.showLoginDialog();
            if (credentials == null) return null;

            // Validar que los campos no estén vacíos
            if (credentials.username.isEmpty() || credentials.password.isEmpty()) {
                ui.showMessage("Username and password cannot be empty.");
                continue;
            }

            // Intentar autenticar
            User user = authService.authenticate(credentials.username, credentials.password, userType);
            if (user != null) {
                return user;
            } else {
                ui.showMessage("Invalid username or password. Please try again.");
            }
        }
    }

    /**
     * Maneja el proceso de cambio de contraseña
     * @param user usuario que necesita cambiar contraseña
     * @return true si el cambio fue exitoso
     */
    private boolean handlePasswordReset(User user) {
        String newPassword = ui.showPasswordChangeDialog();
        if (newPassword == null) return false;

        if (authService.changePassword(user, newPassword, newPassword)) {
            ui.showMessage("Password changed successfully!");
            return true;
        } else {
            ui.showMessage("Password change failed.");
            return false;
        }
    }

    /**
     * Muestra y maneja el menú de administrador
     * @param admin el administrador autenticado
     */
    private void showAdminMenu(Admin admin) {
        while (true) {
            int choice = ui.showAdminMenu(admin.getUsername());

            // Logout
            if (choice == -1 || choice == 4) {
                saveData();
                return;
            }

            switch (choice) {
                case 0: // Crear nuevo usuario
                    handleCreateUser();
                    break;
                case 1: // Listar usuarios
                    ui.showMessage(userService.getUserListString());
                    break;
                case 2: // Eliminar usuario
                    handleRemoveUser();
                    break;
                case 3: // Crear nuevo admin
                    handleCreateAdmin();
                    break;
            }
        }
    }

    /**
     * Maneja la creación de un nuevo usuario
     */
    private void handleCreateUser() {
        User newUser = userService.createUser();
        ui.showMessage("New user created!\nUsername: " + newUser.getUsername() +
                      "\nPassword: " + newUser.getPassword());
        saveData();
    }

    /**
     * Maneja la eliminación de un usuario
     */
    private void handleRemoveUser() {
        if (!userService.hasUsers()) {
            ui.showMessage("No users to remove.");
            return;
        }

        String[] usernames = userService.getUsernames();
        String userToRemove = ui.showUserSelectionDialog(usernames);
        
        if (userToRemove == null) return;

        boolean confirmed = ui.showConfirmDialog(
            "Delete user '" + userToRemove + "'?\nThis will delete all events and wall posts.");

        if (confirmed) {
            userService.removeUser(userToRemove);
            saveData();
            
            // Eliminar datos del usuario del muro y archivos
            deleteUserData(userToRemove);
            
            ui.showMessage("User '" + userToRemove + "' and their data have been deleted.");
        }
    }

    /**
     * Maneja la creación de un nuevo administrador
     */
    private void handleCreateAdmin() {
        Admin newAdmin = adminService.createAdmin();
        ui.showMessage("New admin account created!\nUsername: " + newAdmin.getUsername() +
                      "\nProvisional password: admin123" +
                      "\nThe new admin will be required to set a new password on first login.");
        saveData();
    }

    /**
     * Elimina todos los datos asociados con un usuario
     * @param username nombre del usuario a eliminar
     */
    private void deleteUserData(String username) {
        // Eliminar publicaciones del muro
        SocialCalendarApp.muro.removeIf(publicacion -> publicacion.getUsuario().equals(username));
        SocialCalendarApp.saveMuro();
        
        // Eliminar archivo de eventos del usuario
        String eventoFile = "data/eventos_" + username + ".txt";
        File file = new File(eventoFile);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Guarda todos los datos en el repositorio
     */
    private void saveData() {
        userRepository.save(users, admins, credentialGenerator.getUserCounter());
    }
}
