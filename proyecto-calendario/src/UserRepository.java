// Interface para repositorio de usuarios

import java.util.List;

interface UserRepository {
    // Método para guardar datos de usuarios, admins y contador
    void save(List<User> users, List<Admin> admins, int userCounter);
    
    // Método para cargar datos desde el almacenamiento
    UserData load();
    
    // Clase interna que encapsula los datos cargados
    class UserData {
        public List<User> users;        // Lista de usuarios regulares
        public List<Admin> admins;      // Lista de administradores
        public int userCounter;         // Contador para generar usernames secuenciales
        
        public UserData(List<User> users, List<Admin> admins, int userCounter) {
            this.users = users;
            this.admins = admins;
            this.userCounter = userCounter;
        }
    }
}