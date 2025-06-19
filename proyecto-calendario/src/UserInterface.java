// Interface para UI
interface UserInterface {
    // Muestra el menú principal y retorna la opción seleccionada (0=Admin, 1=User, 2=Exit)
    int showMainMenu();
    
    // Muestra el diálogo de login y retorna las credenciales ingresadas
    LoginCredentials showLoginDialog();
    
    // Muestra un mensaje informativo al usuario
    void showMessage(String message);
    
    // Muestra un diálogo de confirmación (Sí/No) y retorna true si selecciona "Sí"
    boolean showConfirmDialog(String message);
    
    // Muestra diálogo para cambiar contraseña y retorna la nueva contraseña si es válida
    String showPasswordChangeDialog();
    
    // Muestra el menú de administrador con el nombre del admin
    // Retorna la opción seleccionada (0=Crear usuario, 1=Listar, 2=Eliminar, 3=Nuevo admin, 4=Logout)
    int showAdminMenu(String adminName);
    
    // Muestra diálogo de selección de usuario para eliminar
    // Retorna el username seleccionado o null si se cancela
    String showUserSelectionDialog(String[] usernames);
    
    // Clase interna que encapsula las credenciales de login
    class LoginCredentials {
        public String username;    // Nombre de usuario ingresado
        public String password;    // Contraseña ingresada
        
        public LoginCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
