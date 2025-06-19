import java.util.List;

/**
 * Servicio para gestión de usuarios regulares
 * Maneja la creación, eliminación y consulta de usuarios en el sistema
 */
public class UserManagementService {
    private final List<User> users;
    private final CredentialGenerator credentialGenerator;

    /**
     * Constructor del servicio de gestión de usuarios
     * @param users lista de usuarios del sistema
     * @param credentialGenerator generador de credenciales automáticas
     */
    public UserManagementService(List<User> users, CredentialGenerator credentialGenerator) {
        this.users = users;
        this.credentialGenerator = credentialGenerator;
    }

    /**
     * Crea un nuevo usuario con credenciales generadas automáticamente
     * El usuario creado tendrá pendiente el cambio de contraseña en su primer login
     * @return el nuevo usuario creado
     */
    public User createUser() {
        String username = credentialGenerator.generateUsername();
        String password = credentialGenerator.generatePassword();
        
        User newUser = new User(username, password);
        newUser.setPasswordResetPending(true); // Forzar cambio de contraseña en primer login
        users.add(newUser);
        
        return newUser;
    }

    /**
     * Elimina un usuario del sistema basado en su nombre de usuario
     * @param username nombre del usuario a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    public boolean removeUser(String username) {
        return users.removeIf(user -> user.getUsername().equals(username));
    }

    /**
     * Verifica si hay usuarios registrados en el sistema
     * @return true si hay al menos un usuario, false si no hay usuarios
     */
    public boolean hasUsers() {
        return !users.isEmpty();
    }

    /**
     * Obtiene un array con todos los nombres de usuario del sistema
     * @return array de strings con los nombres de usuario
     */
    public String[] getUsernames() {
        return users.stream()
                .map(User::getUsername)
                .toArray(String[]::new);
    }

    /**
     * Genera una cadena formateada con la lista de todos los usuarios
     * Incluye información sobre si tienen pendiente el cambio de contraseña
     * @return string formateado con la información de todos los usuarios
     */
    public String getUserListString() {
        if (users.isEmpty()) {
            return "No users registered in the system.";
        }

        StringBuilder sb = new StringBuilder("=== Registered Users ===\n");
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            sb.append(String.format("%d. Username: %s | Password Reset Pending: %s\n", 
                    i + 1, 
                    user.getUsername(), 
                    user.isPasswordResetPending() ? "Yes" : "No"));
        }
        sb.append("Total users: ").append(users.size());
        
        return sb.toString();
    }

    /**
     * Busca un usuario por su nombre de usuario
     * @param username nombre del usuario a buscar
     * @return el usuario encontrado o null si no existe
     */
    public User findUserByUsername(String username) {
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Obtiene el número total de usuarios en el sistema
     * @return cantidad de usuarios registrados
     */
    public int getUserCount() {
        return users.size();
    }

    /**
     * Verifica si un nombre de usuario ya existe en el sistema
     * @param username nombre de usuario a verificar
     * @return true si el usuario ya existe, false en caso contrario
     */
    public boolean userExists(String username) {
        return users.stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }
}