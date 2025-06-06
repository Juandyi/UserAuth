package calendar;

import javax.swing.*;
import auth.User;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SocialCalendarApp {
    private User user;
    private Properties eventos;
    private Properties muro;
    private final String EVENTOS_FILE = "data\\eventos_"+user.getUsername()+".properties";
    private final String MURO_FILE = "data\\muro_social.properties";

    public SocialCalendarApp(User user) {
        this.user = user;
        eventos = new Properties();
        muro = new Properties();
    }

    public void start() {
        while (true) {
            int option = JOptionPane.showOptionDialog(null,
                    "Bienvenido " + user.getUsername() + " al Calendario Social",
                    "Menú Principal",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new String[]{"Nuevo Evento", "Ver Eventos", "Muro Social", "Salir"},
                    "Nuevo Evento");

            switch(option) {
                case 0: crearEvento(); break;
                case 1: mostrarEventos(); break;
                case 2: gestionarMuro(); break;
                default: return;
            }
        }
    }

    private void crearEvento() {
        JPanel panel = new JPanel(new GridLayout(4, 2));
        JTextField titulo = new JTextField();
        JTextField fecha = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        JTextField hora = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        JTextArea descripcion = new JTextArea(3,20);
        
        panel.add(new JLabel("Título:"));
        panel.add(titulo);
        panel.add(new JLabel("Fecha (YYYY-MM-DD):"));
        panel.add(fecha);
        panel.add(new JLabel("Hora (HH:mm):"));
        panel.add(hora);
        panel.add(new JLabel("Descripción:"));
        panel.add(new JScrollPane(descripcion));

        int result = JOptionPane.showConfirmDialog(null, panel, "Nuevo Evento", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            guardarEvento(titulo.getText(), fecha.getText(), hora.getText(), descripcion.getText());
        }
    }

    private void guardarEvento(String titulo, String fecha, String hora, String descripcion) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String value = String.format("titulo=%s|fecha=%s|hora=%s|desc=%s", 
                titulo, fecha, hora, descripcion);
        
        eventos.setProperty(timestamp, value);
        
        try(FileOutputStream out = new FileOutputStream(EVENTOS_FILE)) {
            eventos.store(out, "Eventos de " + user.getUsername());
            JOptionPane.showMessageDialog(null, "Evento guardado!");
        } catch(IOException e) {
            JOptionPane.showMessageDialog(null, "Error guardando evento: " + e.getMessage());
        }
    }

    private void mostrarEventos() {
        StringBuilder sb = new StringBuilder("<html><div style='font-size:14pt; margin:10px;'>");
        sb.append("<h2>Tus Eventos</h2>");
        
        eventos.stringPropertyNames().stream()
            .sorted(Collections.reverseOrder())
            .forEach(k -> {
                String[] parts = eventos.getProperty(k).split("\\|");
                sb.append(String.format("<p><b>%s</b><br>%s %s<br>%s</p>", 
                    getValue(parts[0]), getValue(parts[1]), getValue(parts[2]), getValue(parts[3])));
            });
        
        sb.append("</div></html>");
        
        JLabel content = new JLabel(sb.toString());
        JOptionPane.showMessageDialog(null, new JScrollPane(content), "Tus Eventos", JOptionPane.PLAIN_MESSAGE);
    }

    private void gestionarMuro() {
        int option = JOptionPane.showOptionDialog(null, 
                "Muro Social - Publica y ve los eventos compartidos",
                "Muro Social",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new String[]{"Nueva Publicación", "Ver Muro", "Volver"},
                "Nueva Publicación");
        
        if(option == 0) nuevaPublicacion();
        else if(option == 1) mostrarMuro();
    }

    private void nuevaPublicacion() {
        String mensaje = JOptionPane.showInputDialog("Escribe tu publicación para el muro:");
        if(mensaje != null && !mensaje.trim().isEmpty()) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String entry = String.format("%s|%s", user.getUsername(), mensaje);
            muro.setProperty(timestamp, entry);
            
            try(FileOutputStream out = new FileOutputStream(MURO_FILE, true)) {
                muro.store(out, "Publicaciones del muro social");
            } catch(IOException e) {
                JOptionPane.showMessageDialog(null, "Error publicando: " + e.getMessage());
            }
        }
    }

    private void mostrarMuro() {
        StringBuilder sb = new StringBuilder("<html><div style='font-size:12pt; margin:10px;'>");
        sb.append("<h2>Últimas Publicaciones</h2>");
        
        muro.stringPropertyNames().stream()
            .sorted(Collections.reverseOrder())
            .limit(20)
            .forEach(k -> {
                String[] parts = muro.getProperty(k).split("\\|");
                sb.append(String.format("<p><b>%s</b> <i>(%s)</i><br>%s</p>", 
                    parts[0], k, parts[1]));
            });
        
        sb.append("</div></html>");
        
        JLabel content = new JLabel(sb.toString());
        JOptionPane.showMessageDialog(null, new JScrollPane(content), "Muro Social", JOptionPane.PLAIN_MESSAGE);
    }

    private String getValue(String keyValue) {
        return keyValue.contains("=") ? keyValue.split("=", 2)[1] : "";
    }
}

