import java.util.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SocialCalendarApp {
    public static List<PublicacionMuro> muro = new ArrayList<>();
    private User currentUser;
    private List<Evento> eventos;
    private Scanner scanner;
    
    // Clase interna para representar un evento
    public static class Evento {
        private String descripcion;
        private LocalDate fecha;
        private LocalTime hora;

        public Evento(String descripcion, LocalDate fecha, LocalTime hora) {
            this.descripcion = descripcion;
            this.fecha = fecha;
            this.hora = hora;
        }

        public String getDescripcion() { return descripcion; }
        public LocalDate getFecha() { return fecha; }
        public LocalTime getHora() { return hora; }

        @Override
        public String toString() {
            DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm");
            return String.format("[%s %s] %s", 
                fecha.format(fechaFormatter), 
                hora.format(horaFormatter), 
                descripcion);
        }

        public String toFileString() {
            DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm");
            return String.format("%s|%s|%s", 
                descripcion, 
                fecha.format(fechaFormatter), 
                hora.format(horaFormatter));
        }

        public static Evento fromFileString(String line) {
            String[] parts = line.split("\\|");
            if (parts.length != 3) return null;
            
            try {
                String descripcion = parts[0];
                LocalDate fecha = LocalDate.parse(parts[1], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                LocalTime hora = LocalTime.parse(parts[2], DateTimeFormatter.ofPattern("HH:mm"));
                return new Evento(descripcion, fecha, hora);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }

    // Clase para las publicaciones del muro con fecha y hora
    public static class PublicacionMuro implements Serializable {
        private static final long serialVersionUID = 1L;
        private String usuario;
        private String mensaje;
        private LocalDate fecha;
        private LocalTime hora;

        public PublicacionMuro(String usuario, String mensaje, LocalDate fecha, LocalTime hora) {
            this.usuario = usuario;
            this.mensaje = mensaje;
            this.fecha = fecha;
            this.hora = hora;
        }

        public String getUsuario() { return usuario; }
        public String getMensaje() { return mensaje; }
        public LocalDate getFecha() { return fecha; }
        public LocalTime getHora() { return hora; }

        @Override
        public String toString() {
            DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm");
            return String.format("%s (%s %s):\n%s", 
                usuario,
                fecha.format(fechaFormatter), 
                hora.format(horaFormatter),
                mensaje);
        }

        public String toFileString() {
            DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm");
            return String.format("%s|%s|%s|%s", 
                usuario,
                mensaje, 
                fecha.format(fechaFormatter), 
                hora.format(horaFormatter));
        }

        public static PublicacionMuro fromFileString(String line) {
            String[] parts = line.split("\\|");
            if (parts.length != 4) return null;
            
            try {
                String usuario = parts[0];
                String mensaje = parts[1];
                LocalDate fecha = LocalDate.parse(parts[2], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                LocalTime hora = LocalTime.parse(parts[3], DateTimeFormatter.ofPattern("HH:mm"));
                return new PublicacionMuro(usuario, mensaje, fecha, hora);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }
    
    public SocialCalendarApp(User user) {
        this.currentUser = user;
        this.eventos = loadEventos();
        this.scanner = new Scanner(System.in);
        loadMuro();
    }
    
    public void start() {
        System.out.println("=== Calendario Social ===");
        System.out.println("Bienvenido, " + currentUser.getUsername() + "!");
        
        while (true) {
            mostrarMenu();
            int opcion = obtenerOpcion();
            
            switch (opcion) {
                case 1:
                    crearEvento();
                    break;
                case 2:
                    mostrarEventos();
                    break;
                case 3:
                    eliminarEvento();
                    break;
                case 4:
                    nuevaPublicacion();
                    break;
                case 5:
                    mostrarMuro();
                    break;
                case 6:
                    System.out.println("¡Hasta luego!");
                    return;
                default:
                    System.out.println("Opción inválida. Intenta de nuevo.");
            }
            
            System.out.println("\nPresiona Enter para continuar...");
            scanner.nextLine();
        }
    }
    
    private void mostrarMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("           MENÚ PRINCIPAL");
        System.out.println("=".repeat(40));
        System.out.println("1. Crear evento");
        System.out.println("2. Ver mis eventos");
        System.out.println("3. Eliminar evento");
        System.out.println("4. Publicar en muro");
        System.out.println("5. Ver muro social");
        System.out.println("6. Salir");
        System.out.println("=".repeat(40));
        System.out.print("Selecciona una opción (1-6): ");
    }
    
    private int obtenerOpcion() {
        try {
            int opcion = Integer.parseInt(scanner.nextLine().trim());
            return opcion;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    public void crearEvento() {
        System.out.println("\n--- Crear Nuevo Evento ---");
        
        // Obtener descripción
        System.out.print("Descripción del evento: ");
        String descripcion = scanner.nextLine().trim();
        if (descripcion.isEmpty()) {
            System.out.println("Error: La descripción no puede estar vacía.");
            return;
        }
        
        // Obtener fecha
        System.out.print("Fecha (dd/mm/yyyy): ");
        String fechaStr = scanner.nextLine().trim();
        if (fechaStr.isEmpty()) {
            System.out.println("Error: La fecha no puede estar vacía.");
            return;
        }
        
        LocalDate fecha;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            fecha = LocalDate.parse(fechaStr, formatter);
        } catch (DateTimeParseException e) {
            System.out.println("Error: Formato de fecha inválido. Usa dd/mm/yyyy");
            return;
        }
        
        // Obtener hora
        System.out.print("Hora (HH:mm - formato 24h): ");
        String horaStr = scanner.nextLine().trim();
        if (horaStr.isEmpty()) {
            System.out.println("Error: La hora no puede estar vacía.");
            return;
        }
        
        LocalTime hora;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            hora = LocalTime.parse(horaStr, formatter);
        } catch (DateTimeParseException e) {
            System.out.println("Error: Formato de hora inválido. Usa HH:mm");
            return;
        }
        
        // Crear y guardar evento
        Evento nuevoEvento = new Evento(descripcion, fecha, hora);
        eventos.add(nuevoEvento);
        
        // Ordenar eventos
        eventos.sort((e1, e2) -> {
            int fechaComparacion = e1.getFecha().compareTo(e2.getFecha());
            if (fechaComparacion == 0) {
                return e1.getHora().compareTo(e2.getHora());
            }
            return fechaComparacion;
        });
        
        saveEventos();
        System.out.println("\n✓ Evento creado exitosamente:");
        System.out.println("  " + nuevoEvento.toString());
    }
    
    public void mostrarEventos() {
        System.out.println("\n--- Mis Eventos ---");
        
        if (eventos.isEmpty()) {
            System.out.println("No tienes eventos registrados.");
            return;
        }
        
        System.out.println("Total de eventos: " + eventos.size());
        System.out.println("-".repeat(50));
        
        for (int i = 0; i < eventos.size(); i++) {
            System.out.printf("%2d. %s%n", (i + 1), eventos.get(i).toString());
        }
    }
    
    public void eliminarEvento() {
        System.out.println("\n--- Eliminar Evento ---");
        
        if (eventos.isEmpty()) {
            System.out.println("No hay eventos para eliminar.");
            return;
        }
        
        mostrarEventos();
        System.out.print("\nIngresa el número del evento a eliminar (0 para cancelar): ");
        
        try {
            int indice = Integer.parseInt(scanner.nextLine().trim());
            
            if (indice == 0) {
                System.out.println("Operación cancelada.");
                return;
            }
            
            if (indice < 1 || indice > eventos.size()) {
                System.out.println("Error: Número de evento inválido.");
                return;
            }
            
            Evento eventoEliminado = eventos.remove(indice - 1);
            saveEventos();
            System.out.println("✓ Evento eliminado: " + eventoEliminado.toString());
            
        } catch (NumberFormatException e) {
            System.out.println("Error: Ingresa un número válido.");
        }
    }
    
    public void nuevaPublicacion() {
        System.out.println("\n--- Publicar en Muro Social ---");
        
        // Obtener mensaje
        System.out.print("Nombre del evento a publicar: ");
        String mensaje = scanner.nextLine().trim();
        if (mensaje.isEmpty()) {
            System.out.println("Error: El mensaje no puede estar vacío.");
            return;
        }
        
        // Obtener fecha del evento
        System.out.print("Fecha del evento (dd/mm/yyyy): ");
        String fechaStr = scanner.nextLine().trim();
        if (fechaStr.isEmpty()) {
            System.out.println("Error: La fecha no puede estar vacía.");
            return;
        }
        
        LocalDate fecha;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            fecha = LocalDate.parse(fechaStr, formatter);
        } catch (DateTimeParseException e) {
            System.out.println("Error: Formato de fecha inválido. Usa dd/mm/yyyy");
            return;
        }
        
        // Obtener hora del evento
        System.out.print("Hora del evento (HH:mm - formato 24h): ");
        String horaStr = scanner.nextLine().trim();
        if (horaStr.isEmpty()) {
            System.out.println("Error: La hora no puede estar vacía.");
            return;
        }
        
        LocalTime hora;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            hora = LocalTime.parse(horaStr, formatter);
        } catch (DateTimeParseException e) {
            System.out.println("Error: Formato de hora inválido. Usa HH:mm");
            return;
        }
        
        // Crear y guardar publicación
        PublicacionMuro nuevaPublicacion = new PublicacionMuro(currentUser.getUsername(), mensaje, fecha, hora);
        muro.add(nuevaPublicacion);
        
        // Ordenar publicaciones por fecha y hora
        muro.sort((p1, p2) -> {
            int fechaComparacion = p1.getFecha().compareTo(p2.getFecha());
            if (fechaComparacion == 0) {
                return p1.getHora().compareTo(p2.getHora());
            }
            return fechaComparacion;
        });
        
        saveMuro();
        System.out.println("✓ Publicación agregada al muro social.");
    }
    
    public void mostrarMuro() {
        System.out.println("\n--- Muro Social ---");
        
        if (muro.isEmpty()) {
            System.out.println("El muro está vacío.");
            return;
        }
        
        System.out.println("Total de publicaciones: " + muro.size());
        System.out.println("=".repeat(60));
        
        for (PublicacionMuro publicacion : muro) {
            System.out.println(publicacion.toString());
            System.out.println("-".repeat(40));
        }
    }
    
    // Método para eliminar eventos y publicaciones de un usuario (para administradores)
    public void eliminarEventoMuro(String userToRemove) {
        // Eliminar publicaciones del muro del usuario
        muro.removeIf(publicacion -> publicacion.getUsuario().equals(userToRemove));
        saveMuro();
        
        // Eliminar archivo de eventos del usuario
        String eventoFile = "data/eventos_" + userToRemove + ".txt";
        File file = new File(eventoFile);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Archivo de eventos eliminado: " + eventoFile);
            } else {
                System.out.println("No se pudo eliminar el archivo: " + eventoFile);
            }
        }
    }
    
    // Métodos de persistencia
    private String getEventoFile() {
        return "data/eventos_" + currentUser.getUsername() + ".txt";
    }
    
    private void saveEventos() {
        try {
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(getEventoFile()))) {
                for (Evento evento : eventos) {
                    writer.println(evento.toFileString());
                }
            }
        } catch (IOException e) {
            System.err.println("Error guardando eventos: " + e.getMessage());
        }
    }
    
    private List<Evento> loadEventos() {
        List<Evento> eventosLoad = new ArrayList<>();
        File file = new File(getEventoFile());
        
        if (!file.exists()) {
            return eventosLoad;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    Evento evento = Evento.fromFileString(linea.trim());
                    if (evento != null) {
                        eventosLoad.add(evento);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error cargando eventos: " + e.getMessage());
        }
        
        // Ordenar eventos después de cargar
        eventosLoad.sort((e1, e2) -> {
            int fechaComparacion = e1.getFecha().compareTo(e2.getFecha());
            if (fechaComparacion == 0) {
                return e1.getHora().compareTo(e2.getHora());
            }
            return fechaComparacion;
        });
        
        return eventosLoad;
    }
    
    @SuppressWarnings("unchecked")
    public static void loadMuro() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("data/muro.ser"))) {
            muro = (List<PublicacionMuro>) ois.readObject();
            
            // Ordenar publicaciones después de cargar
            muro.sort((p1, p2) -> {
                int fechaComparacion = p1.getFecha().compareTo(p2.getFecha());
                if (fechaComparacion == 0) {
                    return p1.getHora().compareTo(p2.getHora());
                }
                return fechaComparacion;
            });
            
        } catch (FileNotFoundException e) {
            muro = new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading muro: " + e.getMessage());
            muro = new ArrayList<>();
        }
    }
    
    public static void saveMuro() {
        try {
            new File("data").mkdirs();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("data/muro.ser"))) {
                oos.writeObject(muro);
            }
        } catch (IOException e) {
            System.err.println("Error saving muro: " + e.getMessage());
        }
    }
}
