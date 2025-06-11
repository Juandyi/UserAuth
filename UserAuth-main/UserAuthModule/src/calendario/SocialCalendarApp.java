package calendario;

import auth.User;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class SocialCalendarApp {
    private User user;
    private java.util.List<Evento> eventos;
    public static java.util.List<PublicacionMuro> muro = new ArrayList<>();

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

    // Nueva clase para las publicaciones del muro con fecha y hora
    public static class PublicacionMuro {
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
        this.user = user;
        this.eventos = loadEventos();
        loadMuro();
    }

    public void start() {
        while (true) {
            String[] options = {
                "Crear evento",
                "Ver mis eventos",
                "Eliminar evento",
                "Publicar en muro",
                "Ver muro social",
                "Salir"
            };

            int choice = JOptionPane.showOptionDialog(
                null,
                "Menú de Calendario Social",
                "Usuario: " + user.getUsername(),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
            );

            if (choice == -1 || choice == 5) break;

            switch (choice) {
                case 0: crearEvento(); break;
                case 1: mostrarEventos(); break;
                case 2: eliminarEvento(); break;
                case 3: nuevaPublicacion(); break;
                case 4: mostrarMuro(); break;
            }
        }
    }
    
    public void eliminarEventoMuro(String userToRemove) {
        // Eliminar publicaciones del muro del usuario
        muro.removeIf(publicacion -> publicacion.getUsuario().equals(userToRemove));
        saveMuro(); // Guardar cambios en el archivo del muro
        
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

    public void crearEvento() {
        // Crear panel simple
        JPanel panel = new JPanel(new GridLayout(6, 1, 5, 5));
        
        // Componentes
        JLabel descripcionLabel = new JLabel("Descripción del evento:");
        JTextField descripcionField = new JTextField(20);
        
        JLabel fechaLabel = new JLabel("Fecha (dd/mm/yyyy):");
        JTextField fechaField = new JTextField(10);
        
        JLabel horaLabel = new JLabel("Hora (HH:mm - formato 24h):");
        JTextField horaField = new JTextField(10);

        // Agregar al panel
        panel.add(descripcionLabel);
        panel.add(descripcionField);
        panel.add(fechaLabel);
        panel.add(fechaField);
        panel.add(horaLabel);
        panel.add(horaField);

        // Mostrar diálogo
        int result = JOptionPane.showConfirmDialog(
            null, 
            panel, 
            "Crear Nuevo Evento", 
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        // Validar descripción
        String descripcion = descripcionField.getText().trim();
        if (descripcion.isEmpty()) {
            JOptionPane.showMessageDialog(null, "La descripción no puede estar vacía.");
            return;
        }

        // Validar fecha
        String fechaStr = fechaField.getText().trim();
        if (fechaStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "La fecha no puede estar vacía.");
            return;
        }

        LocalDate fecha;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            fecha = LocalDate.parse(fechaStr, formatter);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(null, "Formato de fecha inválido. Usa dd/mm/yyyy");
            return;
        }

        // Validar hora
        String horaStr = horaField.getText().trim();
        if (horaStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "La hora no puede estar vacía.");
            return;
        }

        LocalTime hora;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            hora = LocalTime.parse(horaStr, formatter);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(null, "Formato de hora inválido. Usa HH:mm");
            return;
        }

        // Crear evento
        Evento nuevoEvento = new Evento(descripcion, fecha, hora);
        eventos.add(nuevoEvento);
        
        // Ordenar eventos
        eventos.sort(new Comparator<Evento>() {
            public int compare(Evento e1, Evento e2) {
                int fechaComparacion = e1.getFecha().compareTo(e2.getFecha());
                if (fechaComparacion == 0) {
                    return e1.getHora().compareTo(e2.getHora());
                }
                return fechaComparacion;
            }
        });
        
        saveEventos();
        JOptionPane.showMessageDialog(null, "Evento creado:\n" + nuevoEvento.toString());
    }

    public void mostrarEventos() {
        if (eventos.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No tienes eventos.");
        } else {
            StringBuilder sb = new StringBuilder("Tus eventos:\n\n");
            for (int i = 0; i < eventos.size(); i++) {
                sb.append((i + 1)).append(". ").append(eventos.get(i).toString()).append("\n");
            }
            
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            JOptionPane.showMessageDialog(null, scrollPane, "Mis Eventos", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void eliminarEvento() {
        if (eventos.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay eventos para eliminar.");
            return;
        }

        String[] opciones = new String[eventos.size()];
        for (int i = 0; i < eventos.size(); i++) {
            opciones[i] = eventos.get(i).toString();
        }

        String seleccion = (String) JOptionPane.showInputDialog(
            null, 
            "Selecciona el evento a eliminar:",
            "Eliminar Evento", 
            JOptionPane.PLAIN_MESSAGE, 
            null, 
            opciones, 
            opciones[0]
        );

        if (seleccion != null) {
            for (int i = 0; i < eventos.size(); i++) {
                if (eventos.get(i).toString().equals(seleccion)) {
                    eventos.remove(i);
                    break;
                }
            }
            saveEventos();
            JOptionPane.showMessageDialog(null, "Evento eliminado.");
        }
    }

    public void nuevaPublicacion() {
        // Crear panel para la publicación con fecha y hora
        JPanel panel = new JPanel(new GridLayout(8, 1, 5, 5));
        
        // Componentes
        JLabel mensajeLabel = new JLabel("Nombre del evento a publicar en el muro:");
        JTextArea mensajeArea = new JTextArea(3, 20);
        mensajeArea.setLineWrap(true);
        mensajeArea.setWrapStyleWord(true);
        JScrollPane mensajeScroll = new JScrollPane(mensajeArea);
        
        JLabel fechaLabel = new JLabel("Fecha del evento (dd/mm/yyyy):");
        JTextField fechaField = new JTextField(10);
        
        JLabel horaLabel = new JLabel("Hora del evento (HH:mm - formato 24h):");
        JTextField horaField = new JTextField(10);

        // Agregar al panel
        panel.add(mensajeLabel);
        panel.add(mensajeScroll);
        panel.add(new JLabel("")); // Espacio
        panel.add(fechaLabel);
        panel.add(fechaField);
        panel.add(new JLabel("")); // Espacio
        panel.add(horaLabel);
        panel.add(horaField);

        // Mostrar diálogo
        int result = JOptionPane.showConfirmDialog(
            null, 
            panel, 
            "Publicar en Muro Social", 
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        // Validar mensaje
        String mensaje = mensajeArea.getText().trim();
        if (mensaje.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El mensaje no puede estar vacío.");
            return;
        }

        // Validar fecha
        String fechaStr = fechaField.getText().trim();
        if (fechaStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "La fecha no puede estar vacía.");
            return;
        }

        LocalDate fecha;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            fecha = LocalDate.parse(fechaStr, formatter);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(null, "Formato de fecha inválido. Usa dd/mm/yyyy");
            return;
        }

        // Validar hora
        String horaStr = horaField.getText().trim();
        if (horaStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "La hora no puede estar vacía.");
            return;
        }

        LocalTime hora;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            hora = LocalTime.parse(horaStr, formatter);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(null, "Formato de hora inválido. Usa HH:mm");
            return;
        }

        // Crear publicación
        PublicacionMuro nuevaPublicacion = new PublicacionMuro(user.getUsername(), mensaje, fecha, hora);
        muro.add(nuevaPublicacion);
        
        // Ordenar publicaciones por fecha y hora
        muro.sort(new Comparator<PublicacionMuro>() {
            public int compare(PublicacionMuro p1, PublicacionMuro p2) {
                int fechaComparacion = p1.getFecha().compareTo(p2.getFecha());
                if (fechaComparacion == 0) {
                    return p1.getHora().compareTo(p2.getHora());
                }
                return fechaComparacion;
            }
        });
        
        saveMuro();
        JOptionPane.showMessageDialog(null, "Publicado en el muro social.");
    }

    public void mostrarMuro() {
        if (muro.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El muro está vacío.");
        } else {
            StringBuilder sb = new StringBuilder("Muro Social:\n\n");
            for (PublicacionMuro publicacion : muro) {
                sb.append(publicacion.toString()).append("\n\n");
                sb.append("─────────────────────────────────\n\n");
            }
            
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(450, 400));
            JOptionPane.showMessageDialog(null, scrollPane, "Muro Social", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Métodos de persistencia
    private String getEventoFile() {
        return "data/eventos_" + user.getUsername() + ".txt";
    }

    private void saveEventos() {
        try {
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(getEventoFile()))) {
                for (Evento evento : eventos) {
                    writer.println(evento.toFileString());
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error guardando eventos: " + e.getMessage());
        }
    }

    private java.util.List<Evento> loadEventos() {
        java.util.List<Evento> eventosLoad = new ArrayList<>();
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
            JOptionPane.showMessageDialog(null, "Error cargando eventos: " + e.getMessage());
        }
        
        eventosLoad.sort(new Comparator<Evento>() {
            public int compare(Evento e1, Evento e2) {
                int fechaComparacion = e1.getFecha().compareTo(e2.getFecha());
                if (fechaComparacion == 0) {
                    return e1.getHora().compareTo(e2.getHora());
                }
                return fechaComparacion;
            }
        });
        
        return eventosLoad;
    }

    public static void saveMuro() {
        try {
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter("data/muro.txt"))) {
                for (PublicacionMuro publicacion : muro) {
                    writer.println(publicacion.toFileString());
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error guardando muro: " + e.getMessage());
        }
    }

    public static void loadMuro() {
        File file = new File("data/muro.txt");
        
        
        
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linea;
            muro.clear();
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    PublicacionMuro publicacion = PublicacionMuro.fromFileString(linea.trim());
                    if (publicacion != null) {
                        muro.add(publicacion);
                    }
                }
            }
            
            // Ordenar publicaciones después de cargar
            muro.sort(new Comparator<PublicacionMuro>() {
                public int compare(PublicacionMuro p1, PublicacionMuro p2) {
                    int fechaComparacion = p1.getFecha().compareTo(p2.getFecha());
                    if (fechaComparacion == 0) {
                        return p1.getHora().compareTo(p2.getHora());
                    }
                    return fechaComparacion;
                }
            });
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error cargando muro: " + e.getMessage());
        }
    }
}