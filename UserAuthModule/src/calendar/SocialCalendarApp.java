ackage calendario;

import auth.User;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class SocialCalendarApp implements Serializable {
    private static final long serialVersionUID = 1L;

    private User user;
    private List<String> eventos;
    private static List<String> muro = new ArrayList<>();  // Muro común de todos los usuarios

    public SocialCalendarApp(User user) {
        this.user = user;
        this.eventos = loadEventos();  // cargar los eventos previos del usuario
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
            case 0 -> crearEvento();
            case 1 -> mostrarEventos();
            case 2 -> eliminarEvento();
            case 3 -> nuevaPublicacion();
            case 4 -> mostrarMuro();
        }
    }
}


    public void crearEvento() {
        String evento = JOptionPane.showInputDialog("Describe tu evento:");
        if (evento != null && !evento.trim().isEmpty()) {
            eventos.add(evento.trim());
            saveEventos();
            JOptionPane.showMessageDialog(null, "Evento agregado.");
        }
    }

    public void mostrarEventos() {
        if (eventos.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No tienes eventos.");
        } else {
            JOptionPane.showMessageDialog(null, String.join("\n", eventos));
        }
    }

    public void eliminarEvento() {
        if (eventos.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay eventos para eliminar.");
            return;
        }

        String[] opciones = eventos.toArray(new String[0]);
        String seleccion = (String) JOptionPane.showInputDialog(null, "Selecciona el evento a eliminar:",
                "Eliminar Evento", JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);

        if (seleccion != null) {
            eventos.remove(seleccion);
            saveEventos();
            JOptionPane.showMessageDialog(null, "Evento eliminado.");
        }
    }

    public void nuevaPublicacion() {
        String mensaje = JOptionPane.showInputDialog("¿Qué deseas publicar en el muro?");
        if (mensaje != null && !mensaje.trim().isEmpty()) {
            muro.add(user.getUsername() + ": " + mensaje.trim());
            saveMuro();
            JOptionPane.showMessageDialog(null, "Publicado en el muro.");
        }
    }

    public void mostrarMuro() {
        if (muro.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El muro está vacío.");
        } else {
            JOptionPane.showMessageDialog(null, String.join("\n", muro));
        }
    }

    // Archivos de persistencia
    private String getEventoFile() {
        return "data\\eventos_" + user.getUsername() + ".ser";
    }

    private void saveEventos() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(getEventoFile()))) {
            out.writeObject(eventos);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error guardando eventos: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> loadEventos() {
        File file = new File(getEventoFile());
        if (!file.exists()) return new ArrayList<>();

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (List<String>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error cargando eventos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void saveMuro() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("data\\muro.ser"))) {
            out.writeObject(muro);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error guardando muro: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadMuro() {
        File file = new File("data\\muro.ser");
        if (!file.exists()) return;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            muro = (List<String>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error cargando muro: " + e.getMessage());
        }
    }
}
