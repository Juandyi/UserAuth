import java.util.List;

/**
 * Interface para servicios de autenticación
 * Define los contratos para autenticar usuarios y cambiar contraseñas
 */
interface AuthenticationService {
    /**
     * Autentica un usuario con credenciales
     * @param username nombre de usuario
     * @param password contraseña
     * @param userType tipo de usuario ("admin" o "user")
     * @return el usuario autenticado o null si falla
     */
    User authenticate(String username, String password, String userType);
    
    /**
     * Cambia la contraseña de un usuario
     * @param user usuario al que cambiar la contraseña
     * @param newPassword nueva contraseña
     * @param confirmPassword confirmación de la nueva contraseña
     * @return true si el cambio fue exitoso, false si no
     */
    boolean changePassword(User user, String newPassword, String confirmPassword);
}

/**
 * Implementación básica del servicio de autenticación
 * Maneja la autenticación contra listas de usuarios y administradores
 */
class BasicAuthenticationService implements AuthenticationService {
    private final List<User> users;
    private final List<Admin> admins;

    /**
     * Constructor que recibe las listas de usuarios y administradores
     * @param users lista de usuarios regulares
     * @param admins lista de administradores
     */
    public BasicAuthenticationService(List<User> users, List<Admin> admins) {
        this.users = users;
        this.admins = admins;
    }

    /**
     * Autentica un usuario buscando en la lista correspondiente según el tipo
     * @param username nombre de usuario a autenticar
     * @param password contraseña proporcionada
     * @param userType tipo de usuario ("admin" para administradores, cualquier otra cosa para usuarios)
     * @return el usuario autenticado si las credenciales son válidas, null si no
     */
    @Override
    public User authenticate(String username, String password, String userType) {
        // Validar que los parámetros no sean nulos o vacíos
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return null;
        }

        // Seleccionar la lista correcta según el tipo de usuario
        List<? extends User> targetList = "admin".equalsIgnoreCase(userType) ? admins : users;
        
        // Buscar el usuario en la lista correspondiente
        return targetList.stream()
                .filter(user -> user.getUsername().equals(username) && user.authenticate(password))
                .findFirst()
                .orElse(null);
    }

    /**
     * Cambia la contraseña de un usuario después de validar los parámetros
     * @param user usuario al que cambiar la contraseña
     * @param newPassword nueva contraseña
     * @param confirmPassword confirmación de la nueva contraseña
     * @return true si el cambio fue exitoso, false si falló alguna validación
     */
    @Override
    public boolean changePassword(User user, String newPassword, String confirmPassword) {
        // Validar que la nueva contraseña no sea nula o vacía
        if (newPassword == null || newPassword.isEmpty()) {
            return false;
        }
        
        // Validar que las contraseñas coincidan
        if (!newPassword.equals(confirmPassword)) {
            return false;
        }

        // Validar longitud mínima de contraseña (opcional)
        if (newPassword.length() < 3) {
            return false;
        }

        // Cambiar la contraseña y marcar como no pendiente de cambio
        user.setPassword(newPassword);
        user.setPasswordResetPending(false);
        return true;
    }
    
    /**
     * Verifica si un usuario existe en el sistema
     * @param username nombre de usuario a verificar
     * @param userType tipo de usuario ("admin" o "user")
     * @return true si el usuario existe, false si no
     */
    public boolean userExists(String username, String userType) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        
        List<? extends User> targetList = "admin".equalsIgnoreCase(userType) ? admins : users;
        
        return targetList.stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }
    
    /**
     * Obtiene un usuario por su nombre de usuario
     * @param username nombre de usuario a buscar
     * @param userType tipo de usuario ("admin" o "user")
     * @return el usuario encontrado o null si no existe
     */
    public User getUserByUsername(String username, String userType) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        
        List<? extends User> targetList = "admin".equalsIgnoreCase(userType) ? admins : users;
        
        return targetList.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Valida la fortaleza de una contraseña
     * @param password contraseña a validar
     * @return true si la contraseña cumple los criterios, false si no
     */
    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        // Verificar que contenga al menos una letra y un número
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        return hasLetter && hasDigit;
    }
    
    /**
     * Obtiene estadísticas del sistema de autenticación
     * @return información sobre usuarios y administradores
     */
    public String getAuthenticationStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Authentication Statistics:\n");
        stats.append("Total Users: ").append(users.size()).append("\n");
        stats.append("Total Admins: ").append(admins.size()).append("\n");
        
        long usersWithPendingReset = users.stream()
                .mapToLong(user -> user.isPasswordResetPending() ? 1 : 0)
                .sum();
        
        long adminsWithPendingReset = admins.stream()
                .mapToLong(admin -> admin.isPasswordResetPending() ? 1 : 0)
                .sum();
        
        stats.append("Users with pending password reset: ").append(usersWithPendingReset).append("\n");
        stats.append("Admins with pending password reset: ").append(adminsWithPendingReset).append("\n");
        
        return stats.toString();
    }
}
