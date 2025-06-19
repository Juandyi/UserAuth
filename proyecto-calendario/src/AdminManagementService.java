import java.util.List;

/**
 * Servicio para gestión de administradores
 * Maneja la creación, consulta y gestión de cuentas de administrador en el sistema
 */
public class AdminManagementService {
    private final List<Admin> admins;

    /**
     * Constructor del servicio de gestión de administradores
     * @param admins lista de administradores del sistema
     */
    public AdminManagementService(List<Admin> admins) {
        this.admins = admins;
    }

    /**
     * Crea un nuevo administrador con credenciales secuenciales
     * El administrador creado tendrá pendiente el cambio de contraseña en su primer login
     * @return el nuevo administrador creado
     */
    public Admin createAdmin() {
        // Generar nombre de admin secuencial (admin1, admin2, etc.)
        String adminUsername = generateAdminUsername();
        String defaultPassword = "admin123";
        
        Admin newAdmin = new Admin(adminUsername, defaultPassword);
        newAdmin.setPasswordResetPending(true); // Forzar cambio de contraseña
        admins.add(newAdmin);
        
        return newAdmin;
    }

    /**
     * Asegura que existe al menos un administrador por defecto en el sistema
     * Si no hay administradores, crea uno con credenciales por defecto
     */
    public void ensureDefaultAdmin() {
        if (admins.isEmpty()) {
            Admin defaultAdmin = new Admin("admin", "admin123");
            defaultAdmin.setPasswordResetPending(true);
            admins.add(defaultAdmin);
        }
    }

    /**
     * Verifica si hay administradores registrados en el sistema
     * @return true si hay al menos un administrador, false si no hay administradores
     */
    public boolean hasAdmins() {
        return !admins.isEmpty();
    }

    /**
     * Obtiene un array con todos los nombres de usuario de los administradores
     * @return array de strings con los nombres de usuario de administradores
     */
    public String[] getAdminUsernames() {
        return admins.stream()
                .map(Admin::getUsername)
                .toArray(String[]::new);
    }

    /**
     * Genera una cadena formateada con la lista de todos los administradores
     * Incluye información sobre si tienen pendiente el cambio de contraseña
     * @return string formateado con la información de todos los administradores
     */
    public String getAdminListString() {
        if (admins.isEmpty()) {
            return "No administrators registered in the system.";
        }

        StringBuilder sb = new StringBuilder("=== System Administrators ===\n");
        for (int i = 0; i < admins.size(); i++) {
            Admin admin = admins.get(i);
            sb.append(String.format("%d. Username: %s | Password Reset Pending: %s\n", 
                    i + 1, 
                    admin.getUsername(), 
                    admin.isPasswordResetPending() ? "Yes" : "No"));
        }
        sb.append("Total administrators: ").append(admins.size());
        
        return sb.toString();
    }

    /**
     * Elimina un administrador del sistema basado en su nombre de usuario
     * No permite eliminar si es el último administrador del sistema
     * @param username nombre del administrador a eliminar
     * @return true si se eliminó correctamente, false si no se pudo eliminar
     */
    public boolean removeAdmin(String username) {
        if (admins.size() <= 1) {
            return false; // No permitir eliminar el último administrador
        }
        return admins.removeIf(admin -> admin.getUsername().equals(username));
    }

    /**
     * Busca un administrador por su nombre de usuario
     * @param username nombre del administrador a buscar
     * @return el administrador encontrado o null si no existe
     */
    public Admin findAdminByUsername(String username) {
        return admins.stream()
                .filter(admin -> admin.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Obtiene el número total de administradores en el sistema
     * @return cantidad de administradores registrados
     */
    public int getAdminCount() {
        return admins.size();
    }

    /**
     * Verifica si un nombre de usuario de administrador ya existe en el sistema
     * @param username nombre de usuario a verificar
     * @return true si el administrador ya existe, false en caso contrario
     */
    public boolean adminExists(String username) {
        return admins.stream()
                .anyMatch(admin -> admin.getUsername().equals(username));
    }

    /**
     * Genera un nombre de usuario único para un nuevo administrador
     * Sigue el patrón admin1, admin2, admin3, etc.
     * @return nombre de usuario único para administrador
     */
    private String generateAdminUsername() {
        // Si no hay administradores o solo está el admin por defecto, empezar con admin1
        if (admins.isEmpty() || (admins.size() == 1 && admins.get(0).getUsername().equals("admin"))) {
            return "admin1";
        }
        
        // Buscar el siguiente número disponible
        int maxNumber = 0;
        for (Admin admin : admins) {
            String username = admin.getUsername();
            if (username.startsWith("admin") && username.length() > 5) {
                try {
                    int number = Integer.parseInt(username.substring(5));
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException e) {
                    // Ignorar nombres que no sigan el patrón adminX
                }
            }
        }
        
        return "admin" + (maxNumber + 1);
    }

    /**
     * Resetea la contraseña de un administrador a la contraseña por defecto
     * @param username nombre del administrador
     * @return true si se reseteo correctamente, false si no se encontró
     */
    public boolean resetAdminPassword(String username) {
        Admin admin = findAdminByUsername(username);
        if (admin != null) {
            admin.setPassword("admin123");
            admin.setPasswordResetPending(true);
            return true;
        }
        return false;
    }
}